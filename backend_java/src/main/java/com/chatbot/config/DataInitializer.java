package com.chatbot.config;

import com.chatbot.entity.DynamicToolEntity;
import com.chatbot.entity.ToolType;
import com.chatbot.repository.DynamicToolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final DynamicToolRepository toolRepository;

    public DataInitializer(JdbcTemplate jdbcTemplate, DynamicToolRepository toolRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.toolRepository = toolRepository;
    }

    @Override
    public void run(String... args) {
        initWeatherTable();
        initDefaultTools();
    }

    private void initWeatherTable() {
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS weather_logs (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "city VARCHAR(100) NOT NULL, " +
                    "temp_celsius DOUBLE NOT NULL, " +
                    "humidity_percent INT NOT NULL, " +
                    "condition_text VARCHAR(200) NOT NULL, " +
                    "updated_at VARCHAR(50) NOT NULL)");

            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM weather_logs", Integer.class);
            if (count == null || count == 0) {
                jdbcTemplate.execute("INSERT INTO weather_logs (city, temp_celsius, humidity_percent, condition_text, updated_at) VALUES " +
                        "('Hà Nội', 26.5, 78, 'Trời nhiều mây, có lúc có mưa rào nhẹ', '2026-09-13 11:30:00'), " +
                        "('TP. Hồ Chí Minh', 31.0, 72, 'Nắng ấm, chiều tối có thể có mưa dông cục bộ', '2026-09-13 11:30:00'), " +
                        "('Đà Nẵng', 29.2, 70, 'Nắng nhẹ, gió biển mát mẻ', '2026-09-13 11:30:00'), " +
                        "('Hải Phòng', 25.8, 80, 'Trời mát mẻ, nhiều mây', '2026-09-13 11:30:00'), " +
                        "('Cần Thơ', 30.5, 75, 'Nắng ráo, thời tiết thuận lợi', '2026-09-13 11:30:00')");
                log.info("[DataInitializer] Đã khởi tạo bảng weather_logs mẫu trong H2 Database.");
            }
        } catch (Exception e) {
            log.warn("[DataInitializer] Lỗi tạo bảng weather_logs: {}", e.getMessage());
        }
    }

    private void initDefaultTools() {
        if (toolRepository.count() > 0) return;

        log.info("[DataInitializer] Đang khởi tạo 3 Tool mẫu ban đầu vào H2 Database...");

        // 1. Tool Canteen Menu (STATIC_MARKDOWN)
        DynamicToolEntity canteenTool = new DynamicToolEntity();
        canteenTool.setName("get_canteen_menu");
        canteenTool.setDescription("Tra cứu thực đơn nhà ăn / canteen của công ty theo ngày hôm nay hoặc ngày cụ thể.");
        canteenTool.setToolType(ToolType.STATIC_MARKDOWN);
        canteenTool.setParametersSchema("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"date\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Ngày cần tra cứu thực đơn (ví dụ: 'today', 'hôm nay', hoặc YYYY-MM-DD)\"\n" +
                "    }\n" +
                "  }\n" +
                "}");
        canteenTool.setConfigData("### 🍽️ Thực đơn Canteen Nhà ăn Công ty hôm nay:\n" +
                "- **Món chính (Món mặn)**: Cơm tấm sườn cốt lết nướng mật ong, Cá thu kho tiêu, Đậu hũ dồn thịt sốt cà chua.\n" +
                "- **Món xào & Rau**: Bông cải xanh xào nấm đông cô, Rau muống xào tỏi thơm giòn.\n" +
                "- **Món canh**: Canh chua cá bớp nấu thơm bạc hà, Canh bí đao hầm sườn non.\n" +
                "- **Món tráng miệng & Đồ uống**: Chè hạt sen nhãn nhục, Dưa hấu ướp lạnh, Trà sâm giải nhiệt.\n" +
                "- *Giờ phục vụ bữa trưa*: 11:15 - 13:30 tại Tầng 2 Tòa nhà Văn phòng.");
        canteenTool.setEnabled(true);
        toolRepository.save(canteenTool);

        // 2. Tool Thời tiết (SQL_QUERY)
        DynamicToolEntity weatherTool = new DynamicToolEntity();
        weatherTool.setName("get_weather_info");
        weatherTool.setDescription("Tra cứu thông tin thời tiết, nhiệt độ (°C), độ ẩm (%) và hiện tượng khí tượng tại các thành phố từ cơ sở dữ liệu.");
        weatherTool.setToolType(ToolType.SQL_QUERY);
        weatherTool.setParametersSchema("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"city\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Tên thành phố hoặc tỉnh thành (ví dụ: Hà Nội, TP. Hồ Chí Minh, Đà Nẵng, Hải Phòng)\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"city\"]\n" +
                "}");
        weatherTool.setConfigData("SELECT city AS \"Thành phố\", temp_celsius AS \"Nhiệt độ (°C)\", humidity_percent AS \"Độ ẩm (%)\", condition_text AS \"Tình trạng\", updated_at AS \"Cập nhật\" FROM weather_logs WHERE LOWER(city) LIKE LOWER('%' || :city || '%') ORDER BY id DESC LIMIT 5");
        weatherTool.setEnabled(true);
        toolRepository.save(weatherTool);

        // 3. Tool Tài liệu Sản xuất & Luật lao động (QDRANT_VECTOR)
        DynamicToolEntity qdrantTool = new DynamicToolEntity();
        qdrantTool.setName("search_production_legal");
        qdrantTool.setDescription("Tìm kiếm thông tin quy chuẩn kỹ thuật nhà máy sản xuất, quy trình an toàn lao động ISO hoặc luật lao động hiện hành.");
        qdrantTool.setToolType(ToolType.QDRANT_VECTOR);
        qdrantTool.setParametersSchema("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"query\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"Từ khóa hoặc câu hỏi cần tra cứu tài liệu quy chuẩn/luật pháp\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"query\"]\n" +
                "}");
        qdrantTool.setConfigData("{\"endpoint\": \"http://localhost:6333\", \"collection\": \"production_legal\"}");
        qdrantTool.setEnabled(true);
        toolRepository.save(qdrantTool);

        log.info("[DataInitializer] Khởi tạo thành công 3 Tool mẫu (get_canteen_menu, get_weather_info, search_production_legal).");
    }
}

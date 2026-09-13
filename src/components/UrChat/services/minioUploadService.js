/**
 * MinIO Object Storage Service: Upload & Xóa ảnh qua Backend API
 */

export async function uploadImageToMinio(base64Data, fileName) {
  try {
    const res = await fetch('/api/upload', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        image: base64Data,
        name: fileName
      })
    });
    const data = await res.json();
    if (!res.ok || !data.success) {
      throw new Error(data.error || 'Lỗi upload ảnh lên MinIO');
    }
    return {
      url: data.url,
      key: data.key
    };
  } catch (err) {
    console.error('[MinioUploadService] Lỗi upload ảnh:', err);
    throw err;
  }
}

export async function deleteImageFromMinio(key) {
  if (!key) return;
  try {
    const res = await fetch('/api/upload', {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ key })
    });
    return await res.json();
  } catch (err) {
    console.warn('[MinioUploadService] Lỗi xóa ảnh:', err);
  }
}

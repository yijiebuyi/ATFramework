package com.callme.platform.widget.crop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.callme.platform.glsrender.core.Utils;
import com.callme.platform.util.thdpool.ThreadPool;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：
 * 作者：huangyong
 * 创建时间：2018/1/25
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class NetImageRequest implements ThreadPool.Job<Bitmap> {
    private String mNetPath;
    private int mType = MediaItem.TYPE_MICROTHUMBNAIL;
    private static final int TARGET_SIZE = 1080;

    public NetImageRequest(String path, int type) {
        mNetPath = path;
        mType = type;
    }

    @Override
    public Bitmap run(ThreadPool.JobContext jc) {
        Bitmap bitmap = null;
        try {
            byte[] data = getImage(mNetPath);
            if (data != null) {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);// bitmap
            }
            //saveFile(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Get image from newwork
     *
     * @param path The path of image
     * @return byte[]
     * @throws Exception
     */
    public byte[] getImage(String path) {
        InputStream inStream = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            inStream = conn.getInputStream();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return readStream(inStream);
            }
        } catch (IOException e) {

        } finally {
            Utils.closeSilently(inStream);
            // disconnect connection
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    /**
     * Get data from stream
     *
     * @param inStream
     * @return byte[]
     * @throws Exception
     */
    public byte[] readStream(InputStream inStream) {
        ByteArrayOutputStream outStream = null;
        try {
            outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            return outStream.toByteArray();
        } catch (IOException e) {
            return null;
        } finally {
            Utils.closeSilently(outStream);
        }
    }


    /**
     * 保存文件
     *
     * @param bm
     * @throws IOException
     */
    public void saveFile(Bitmap bm) {
        BufferedOutputStream bos = null;
        try {
            String fileName = mNetPath.hashCode() + ".jpg";
            File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/Crop/");
            if (!folder.exists()) {   //判断文件夹是否存在，不存在则创建
                folder.mkdirs();
            }
            File myCaptureFile = new File(folder + "/" + fileName);
            bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {

        } finally {
            Utils.closeSilently(bos);
        }
    }
}

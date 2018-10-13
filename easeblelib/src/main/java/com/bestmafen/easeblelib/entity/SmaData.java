package com.bestmafen.easeblelib.entity;

import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class SmaData {
    public static final int ENDIAN_BIG    = 0;
    public static final int ENDIAN_LITTLE = 1;

    @IntDef({ENDIAN_BIG, ENDIAN_LITTLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Endian {

    }

    private byte[] mData;//数据源
    private int   mOffset   = -1;//已读取/写入的位置
    private int[] mPosition = new int[2];//下个要读取/写入的位置，[0]为第几个byte，[1]为byte的第几位

    @IntRange(from = 1)
    public abstract int size();

    public byte[] getData() {
        return getData(true);
    }

    public byte[] getData(boolean decode) {
        if (decode) {
            decode();
        }
        return mData;
    }

    @CallSuper
    public void encode(byte[] data) {
        if (data == null || data.length < 1 || data.length != size()) return;

        reset();
        mData = data;
    }

    @CallSuper
    public void decode() {
        if (mData == null || mData.length != size()) {
            mData = new byte[size()];
        } else {
            Arrays.fill(mData, (byte) 0);
        }
        reset();
    }

    private void reset() {
        mOffset = -1;
        Arrays.fill(mPosition, 0);
    }

    /**
     * 检测如果读取指定位，是否会越界
     *
     * @param bits 位数
     * @return true:没有越界;false:已越界
     */
    private boolean withinRange(int bits) {
        return mData != null && (mOffset + bits) <= (mData.length * 8 - 1);
    }

    /**
     * 移动读取位置
     *
     * @param bits 位数，正向后跳，负向前跳
     */
    public void skip(int bits) {
        if (mData == null) return;

        mOffset += bits;
        if (mOffset < -1) {
            mOffset = -1;
        }

        int offset = mOffset + 1;
        mPosition[0] = offset / 8;
        mPosition[1] = offset % 8;
    }

    /**
     * 读取数据源的下1位
     */
    public byte readBoolean() {
        if (withinRange(1)) {
            byte result = (byte) ((mData[mPosition[0]] >> (7 - mPosition[1])) & 0x01);
            skip(1);
            return result;
        }

        return 0;
    }

    /**
     * 读取占n位的带符号byte值
     *
     * @param n 读取的位数
     * @return 读取到的值
     */
    public byte readIntN(@IntRange(from = 1, to = 8) int n) {
        if (n < 1 || n > 8 || !withinRange(n)) return -1;

        int value;
        if (mPosition[1] + n <= 8) {//没有跨字节
            value = (mData[mPosition[0]]) >> (8 - mPosition[1] - n);
            value &= (int) Math.pow(2, n) - 1;
        } else {//已经跨字节
            value = mData[mPosition[0]] & ((int) Math.pow(2, 8 - mPosition[1]) - 1);
            value <<= (mPosition[1] + n - 8);
            int value2 = mData[mPosition[0] + 1] & 0xff;
            value2 >>= (16 - mPosition[1] - n);
            value |= value2;
        }
        skip(n);
        return (byte) value;
    }

    /**
     * 读取数据源的下8位，解析成带符号int8
     */
    public byte readInt8() {
        return readIntN(8);
    }

    /**
     * 读取数据源的下16位，解析成带符号int16
     */
    public short readInt16(@Endian int endian) {
        byte byte1 = readInt8();
        byte byte2 = readInt8();
        if (endian == ENDIAN_BIG) {
            return (short) ((byte1 << 8) | (byte2 & 0xff));
        } else {
            return (short) ((byte2 << 8) | (byte1 & 0xff));
        }
    }

    /**
     * 大端序读取数据源的下16位，解析成带符号int16
     */
    public short readInt16() {
        return readInt16(ENDIAN_BIG);
    }

    /**
     * 读取数据源的下32位，解析成带符号int32
     */
    public int readInt32() {
        short short1 = readInt16();
        short short2 = readInt16();
        return (short1 << 16) | (short2 & 0xffff);
    }

    /**
     * 大端序读取数据源的下64位，解析成带符号int64
     */
    public long readInt64() {
        return readInt64(ENDIAN_BIG);
    }

    public long readInt64(@Endian int endian) {
        if (endian == ENDIAN_BIG) {
            short short1 = readInt16();
            short short2 = readInt16();
            short short3 = readInt16();
            short short4 = readInt16();
            return ((long) short1 << 48) | (((long) short2 & 0xffff) << 32) | (((long) short3 & 0xffff) << 16) | (short4 & 0xffff);
        } else {
            short short1 = readInt16(ENDIAN_LITTLE);
            short short2 = readInt16(ENDIAN_LITTLE);
            short short3 = readInt16(ENDIAN_LITTLE);
            short short4 = readInt16(ENDIAN_LITTLE);
            return ((long) short4 << 48) | (((long) short3 & 0xffff) << 32) | (((long) short2 & 0xffff) << 16) | (short1 & 0xffff);
        }
    }

    /**
     * 读取占n位的无符号byte值
     *
     * @param n 读取的位数
     * @return 读取到的值
     */
    public short readUintN(@IntRange(from = 1, to = 8) int n) {
        return (short) (readIntN(n) & 0xff);
    }

    /**
     * 读取数据源的下8位，解析成无符号int8
     */
    public short readUint8() {
        return readUintN(8);
    }

    public char readUint16(@Endian int endian) {
        if (endian == ENDIAN_BIG) {
            return (char) (readInt16() & 0xffff);
        } else {
            byte byte1 = readInt8();
            byte byte2 = readInt8();
            return (char) (((byte2 & 0xff) << 8) | (byte1 & 0xff));
        }
    }

    /**
     * 读取数据源的下16位，解析成无符号int16
     */
    public char readUint16() {
        return readUint16(ENDIAN_BIG);
    }

    /**
     * 读取数据源的下24位，解析成int32，必定是正数
     */
    public int readUint24() {
        short short1 = readUint8();
        short short2 = readInt16();
        return (short1 << 16) | (short2 & 0xffff);
    }

    /**
     * 读取数据源的下32位，解析成无符号int32
     */
    public long readUint32(@Endian int endian) {
        if (endian == ENDIAN_BIG) {
            char char1 = readUint16();
            char char2 = readUint16();
            return (((long) char1 & 0xffff) << 16) | (char2 & 0xffff);
        } else {
            char char1 = readUint16(ENDIAN_LITTLE);
            char char2 = readUint16(ENDIAN_LITTLE);
            return (((long) char2 & 0xffff) << 16) | (char1 & 0xffff);
        }
    }

    /**
     * 读取数据源的下32位，解析成无符号int32
     */
    public long readUint32() {
        return readUint32(ENDIAN_BIG);
    }

    /**
     * 读取数据源的下32位，解析成float
     */
    public float readFloat() {
        int intBits = readInt32();
        return Float.intBitsToFloat(intBits);
    }

    /**
     * 读取数据源的下64位，解析成double
     */
    public double readDouble() {
        long longBits = readInt64();
        return Double.longBitsToDouble(longBits);
    }

    public byte[] readByteArray(int length) {
        byte[] bytes = new byte[length];
        if (mPosition[1] == 0) {
            System.arraycopy(mData, mPosition[0], bytes, 0, length);
            skip(length * 8);
        } else {
            for (int i = 0; i < length; i++) {
                bytes[i] = readInt8();
            }
        }
        return bytes;
    }

    public String readString(int length, String charSet) {
        try {
            return new String(readByteArray(length), charSet);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public <T extends SmaData> T readBuffer(Class<T> t) {
        T item = null;
        try {
            item = t.newInstance();
            int len = item.size();
            item.encode(readByteArray(len));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return item;
    }

    @SuppressWarnings("unchecked")
    public <T extends SmaData> T[] readBufferList(Class<T> t, int count) {
        T[] buffers = (T[]) Array.newInstance(t, count);
        for (int i = 0; i < count; i++) {
            buffers[i] = readBuffer(t);
        }
        return buffers;
    }

    public static <T extends SmaData> T encodeItem(byte[] bytes, Class<T> t) {
        try {
            T item = t.newInstance();
            item.encode(bytes);
            return item;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static <T extends SmaData> List<T> encodeList(byte[] bytes, Class<T> t) {
        try {
            T item = t.newInstance();
            int size = item.size();
            if (bytes == null || bytes.length == 0 || bytes.length % size != 0)
                return Collections.emptyList();

            List<T> list = new ArrayList<>();
            int count = bytes.length / size;
            for (int i = 0; i < count; i++) {
                item = t.newInstance();
                item.encode(Arrays.copyOfRange(bytes, size * i, size * (i + 1)));
                list.add(item);
            }
            return list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 写入占n位的int值
     *
     * @param value 待写入的值
     * @param n     占用的位数
     */
    public void writeIntN(int value, @IntRange(from = 1, to = 8) int n) {
        if (n < 1 || n > 8 || !withinRange(n)) return;

        //截取value的后n位
        value &= (int) (Math.pow(2, n) - 1);
        if (mPosition[1] + n <= 8) {//没有跨字节
            int shift = 8 - (mPosition[1] + n);
            mData[mPosition[0]] |= (value << shift);
        } else {//已经跨字节
            int shift = mPosition[1] + n - 8;//跨过的位数
            mData[mPosition[0]] |= (value >> shift);
            mData[mPosition[0] + 1] |= (value & (int) (Math.pow(2, shift) - 1)) << (8 - shift);
        }
        skip(n);
    }

    /**
     * 写入一个int8
     *
     * @param value 待写入的值
     */
    public void writeInt8(int value) {
        writeIntN(value & 0xff, 8);
    }

    /**
     * 写入一个int16
     *
     * @param value  待写入的值
     * @param endian 大端序 or 小端序
     */
    public void writeInt16(int value, @Endian int endian) {
        switch (endian) {
            case ENDIAN_BIG:
                writeInt8((value >> 8) & 0xff);
                writeInt8(value & 0xff);
                break;
            case ENDIAN_LITTLE:
                writeInt8(value & 0xff);
                writeInt8((value >> 8) & 0xff);
                break;
        }
    }

    /**
     * 大端序写入一个int16
     *
     * @param value 待写入的值
     */
    public void writeInt16(int value) {
        writeInt16(value, ENDIAN_BIG);
    }

    /**
     * 写入一个带符号int32
     *
     * @param value  待写入的值
     * @param endian 大端序 or 小端序
     */
    public void writeInt32(int value, @Endian int endian) {
        switch (endian) {
            case ENDIAN_BIG:
                writeInt8((value >> 24) & 0xff);
                writeInt8((value >> 16) & 0xff);
                writeInt8((value >> 8) & 0xff);
                writeInt8(value & 0xff);
                break;
            case ENDIAN_LITTLE:
                writeInt8(value & 0xff);
                writeInt8((value >> 16) & 0xff);
                writeInt8((value >> 24) & 0xff);
                writeInt8((value >> 24) & 0xff);
                break;
        }
    }

    public void writeInt32(int value) {
        writeInt32(value, ENDIAN_BIG);
    }

    /**
     * 写入一个无符号int32
     *
     * @param value  待写入的值
     * @param endian 大端序 or 小端序
     */
    public void writeUint32(long value, @Endian int endian) {
        int intValue = (int) (value);
        switch (endian) {
            case ENDIAN_BIG:
                writeInt8((intValue >> 24) & 0xff);
                writeInt8((intValue >> 16) & 0xff);
                writeInt8((intValue >> 8) & 0xff);
                writeInt8(intValue & 0xff);
                break;
            case ENDIAN_LITTLE:
                writeInt8(intValue & 0xff);
                writeInt8((intValue >> 16) & 0xff);
                writeInt8((intValue >> 24) & 0xff);
                writeInt8((intValue >> 24) & 0xff);
                break;
        }
    }

    public void writeUint32(long value) {
        writeUint32(value, ENDIAN_BIG);
    }

    /**
     * 写入一个带符号int64
     *
     * @param value  待写入的值
     * @param endian 大端序 or 小端序
     */
    public void writeInt64(long value, @Endian int endian) {
        switch (endian) {
            case ENDIAN_BIG:
                writeInt8((int) ((value >> 56) & 0xff));
                writeInt8((int) ((value >> 48) & 0xff));
                writeInt8((int) ((value >> 40) & 0xff));
                writeInt8((int) ((value >> 32) & 0xff));
                writeInt8((int) ((value >> 24) & 0xff));
                writeInt8((int) ((value >> 16) & 0xff));
                writeInt8((int) ((value >> 8) & 0xff));
                writeInt8((int) (value & 0xff));
                break;
            case ENDIAN_LITTLE:
                writeInt8((int) (value & 0xff));
                writeInt8((int) ((value >> 16) & 0xff));
                writeInt8((int) ((value >> 24) & 0xff));
                writeInt8((int) ((value >> 24) & 0xff));
                writeInt8((int) ((value >> 32) & 0xff));
                writeInt8((int) ((value >> 40) & 0xff));
                writeInt8((int) ((value >> 48) & 0xff));
                writeInt8((int) ((value >> 56) & 0xff));
                break;
        }
    }

    /**
     * 写入byte数组
     *
     * @param bytes  待写入的byte数组
     * @param endian 大端序 or 小端序
     */
    public void writeByteArray(byte[] bytes, @Endian int endian) {
        if (bytes == null || bytes.length == 0) return;

        switch (endian) {
            case ENDIAN_BIG:
                for (byte item : bytes) {
                    writeInt8(item);
                }
                break;
            case ENDIAN_LITTLE:
                for (int i = 0, l = bytes.length; i < l; i++) {
                    writeInt8(bytes[l - 1 - i]);
                }
                break;
        }
    }

    /**
     * 大端序写入byte数组
     *
     * @param bytes 待写入的byte数组
     */
    public void writeByteArray(byte[] bytes) {
        writeByteArray(bytes, ENDIAN_BIG);
    }

    /**
     * 大端序写入字符串，限定最大长度，如果没有超过限定长度，编码后是多长写入多长
     *
     * @param text    待写入的字符串
     * @param charSet 编码格式
     * @param limit   限定最大分配的字节数
     */
    public void writeStringWithLimit(String text, String charSet, int limit) {
        if (TextUtils.isEmpty(text)) return;

        //skip(8 - mPosition[1]);//写入的字符序列不允许跨字节，先跳到下个字节的开始位置
        try {
            byte[] textArray = text.getBytes(charSet);
            int length = textArray.length < limit ? textArray.length : limit;
            for (int i = 0; i < length; i++) {
                writeInt8(textArray[i]);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 大端序写入字符串，不管编码后多长，都分配固定长度
     *
     * @param text    待写入的字符串
     * @param charSet 编码格式
     * @param fix     分配的固定长度
     */
    public void writeStringWithFix(String text, String charSet, int fix) {
        if (TextUtils.isEmpty(text)) {
            skip(fix * 8);
            return;
        }

        try {
            byte[] textArray = text.getBytes(charSet);
            int length = textArray.length < fix ? textArray.length : fix;
            for (int i = 0; i < length; i++) {
                writeInt8(textArray[i]);
            }
            if (textArray.length < fix) {
                skip((fix - textArray.length) * 8);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 大端序写入字符串，编码后是多长，就写多长
     *
     * @param text    待写入的字符串
     * @param charSet 编码格式
     */
    public void writeString(String text, String charSet) {
        if (TextUtils.isEmpty(text)) return;

        try {
            byte[] textArray = text.getBytes(charSet);
            for (byte b : textArray) {
                writeInt8(b);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}

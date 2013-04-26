package org.eurekaj.plugins.util;

/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 2/26/13
 * Time: 2:31 PM
 * To change this template use File | Settings | File Templates.
 */

import me.prettyprint.cassandra.serializers.AbstractSerializer;
import me.prettyprint.cassandra.serializers.ObjectSerializer;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.exceptions.HectorSerializationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * A multi-type Hector-Cassandra serializer.
 *
 * Can be used for both reads and writes.
 * Does not require prior knowledge of the type to be unserialized.
 * Useful for reading and writing to/from multi-type structures (e.g. a JSON object)
 *
 * @author David Semeria
 */
@SuppressWarnings("unused")
public class JSerializer extends AbstractSerializer<Object> implements Serializer<Object>{

    private static final JSerializer instance = new JSerializer();
    private static final Serializer<Object> objSerialiazer = new ObjectSerializer();

    private static final String  UTF_8   = "UTF-8";
    private static final Charset charset = Charset.defaultCharset();

    private static final byte TYPE_NULL    = 0;
    private static final byte TYPE_CHAR    = 1;
    private static final byte TYPE_STRING  = 2;
    private static final byte TYPE_BOOLEAN = 3;
    private static final byte TYPE_INT     = 4;
    private static final byte TYPE_LONG    = 5;
    private static final byte TYPE_FLOAT   = 6;
    private static final byte TYPE_DOUBLE  = 7;
    private static final byte TYPE_UUID    = 10;
    private static final byte TYPE_OBJECT  = 50;

    /**
     * Returns an instance of JSerializer
     * @return the instance
     */
    public static JSerializer get() {
        return instance;
    }

    /**
     * Returns a byte identifying the value type
     *
     * @param value the object to check
     * @return a byte identifying the type
     */
    public static byte type(Object value){
        if (value == null)                 {return TYPE_NULL;}
        if (value instanceof Character)    {return TYPE_CHAR;}
        if (value instanceof String)       {return TYPE_STRING;}
        if (value instanceof Integer)      {return TYPE_INT;}
        if (value instanceof Float)        {return TYPE_FLOAT;}
        if (value instanceof Long)         {return TYPE_LONG;}
        if (value instanceof Double)       {return TYPE_DOUBLE;}
        if (value instanceof Boolean)      {return TYPE_BOOLEAN;}
        if (value instanceof UUID)         {return TYPE_UUID;}
        return TYPE_OBJECT;
    }

    /**
     * @see me.prettyprint.cassandra.serializers.AbstractSerializer#fromByteBuffer(java.nio.ByteBuffer)
     */
    @Override
    public Object fromByteBuffer(ByteBuffer bb) {
        if (bb.capacity() == 0){
            return null;
        }

        bb.position(bb.position()+1);

        switch(bb.get(0)){
            case TYPE_NULL:    return null;
            case TYPE_CHAR:    return charset.decode(bb).toString().charAt(0);
            case TYPE_STRING:  return charset.decode(bb).toString();
            case TYPE_BOOLEAN: return bb.get() == (byte)1;
            case TYPE_INT:     return bb.getInt();
            case TYPE_LONG:    return bb.getLong();
            case TYPE_FLOAT:   return bb.getFloat();
            case TYPE_DOUBLE:  return bb.getDouble();
            case TYPE_UUID:    return new UUID(bb.getLong(), bb.getLong());
        }
        return objSerialiazer.fromByteBuffer(bb);
    }

    /**
     * @see me.prettyprint.cassandra.serializers.AbstractSerializer#toByteBuffer(java.lang.Object)
     */
    @Override
    public ByteBuffer toByteBuffer(Object obj) {
        switch (type(obj)){
            case TYPE_CHAR:    return charToByteBuffer((Character) obj);
            case TYPE_STRING:  return stringToByteBuffer((String) obj);
            case TYPE_BOOLEAN: return booleanToByteBuffer((Boolean) obj);
            case TYPE_INT:     return intToByteBuffer((Integer) obj);
            case TYPE_LONG:    return longToByteBuffer((Long) obj);
            case TYPE_FLOAT:   return floatToByteBuffer((Float) obj);
            case TYPE_DOUBLE:  return doubleToByteBuffer((Double) obj);
            case TYPE_UUID:    return UUIDtoByteBuffer((UUID) obj);
        }
        return objectToByteBuffer(obj);
    }

    private ByteBuffer booleanToByteBuffer(Boolean val) {
        byte[] b = new byte[2];
        b[0] = TYPE_BOOLEAN;
        b[1] = val ?(byte) 1 :(byte) 0;
        return ByteBuffer.wrap(b);
    }

    private ByteBuffer charToByteBuffer(Character val){
        return stringToByteBuffer(new String(new char[] {val}), TYPE_CHAR);
    }

    private ByteBuffer stringToByteBuffer(String val){
        return stringToByteBuffer(val, TYPE_STRING);
    }

    private ByteBuffer stringToByteBuffer(String val, byte type){
        try{
            byte[] bx = val.getBytes(charset.name());
            byte[] by = new byte[bx.length+1];
            by[0] = type;
            System.arraycopy(bx, 0, by, 1, bx.length);
            return ByteBuffer.wrap(by);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private ByteBuffer intToByteBuffer(Integer val){
        ByteBuffer bb = ByteBuffer.allocate(5);
        bb.put(0,TYPE_INT);
        bb.putInt(1,val);
        bb.rewind();
        return bb;
    }

    private ByteBuffer longToByteBuffer(Long val){
        ByteBuffer bb = ByteBuffer.allocate(9);
        bb.put(0,TYPE_LONG);
        bb.putLong(1,val);
        bb.rewind();
        return bb;
    }

    private ByteBuffer floatToByteBuffer(float val){
        ByteBuffer bb = ByteBuffer.allocate(5);
        bb.put(0,TYPE_FLOAT);
        bb.putFloat(1,val);
        bb.rewind();
        return bb;
    }

    private ByteBuffer doubleToByteBuffer(double val){
        ByteBuffer bb = ByteBuffer.allocate(9);
        bb.put(0,TYPE_DOUBLE);
        bb.putDouble(1,val);
        bb.rewind();
        return bb;
    }

    private ByteBuffer UUIDtoByteBuffer(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] b = new byte[17];

        b[0] = TYPE_UUID;
        for (int i = 0; i < 8; i++) {
            b[i+1] = (byte) (msb >>> 8 * (7 - i));
        }
        for (int i = 8; i < 16; i++) {
            b[i+1] = (byte) (lsb >>> 8 * (7 - i));
        }

        return ByteBuffer.wrap(b);
    }

    private ByteBuffer objectToByteBuffer(Object obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            byte[] bx = baos.toByteArray();
            byte[] by = new byte[bx.length+1];
            by[0] = TYPE_OBJECT;
            System.arraycopy(bx, 0, by, 1, bx.length);
            return ByteBuffer.wrap(by);
        } catch (IOException
                ex) {
            throw new HectorSerializationException(ex);
        }
    }

}

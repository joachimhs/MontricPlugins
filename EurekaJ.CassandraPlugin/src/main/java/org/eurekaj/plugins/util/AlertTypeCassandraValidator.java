package org.eurekaj.plugins.util;

import me.prettyprint.cassandra.serializers.StringSerializer;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.MarshalException;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.eurekaj.api.enumtypes.AlertType;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 2/26/13
 * Time: 4:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlertTypeCassandraValidator extends AbstractType<AlertType> {
    public static final AlertTypeCassandraValidator instance = new AlertTypeCassandraValidator();

    @Override
    public AlertType compose(ByteBuffer byteBuffer) {
        return AlertType.fromValue(StringSerializer.get().fromByteBuffer(byteBuffer));
    }

    @Override
    public ByteBuffer decompose(AlertType alertType) {
        return StringSerializer.get().toByteBuffer(alertType.getTypeName());
    }

    @Override
    public String getString(ByteBuffer byteBuffer) {
        return StringSerializer.get().fromByteBuffer(byteBuffer);
    }

    @Override
    public ByteBuffer fromString(String s) throws MarshalException {
        return StringSerializer.get().toByteBuffer(s);
    }

    @Override
    public void validate(ByteBuffer byteBuffer) throws MarshalException {

    }

    @Override
    public int compare(ByteBuffer o1, ByteBuffer o2) {
        if (o1.remaining() == 0) {
            return o2.remaining() == 0 ? 0 : -1;
        }

        if (o2.remaining() == 0) {
            return 1;
        }

        int diff = o1.get(o1.position()) - o2.get(o2.position());
        if (diff != 0) {
            return diff;
        }

        return ByteBufferUtil.compareUnsigned(o1, o2);
    }
}

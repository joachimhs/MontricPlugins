package org.eurekaj.plugins.util;

import me.prettyprint.cassandra.serializers.StringSerializer;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.MarshalException;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.eurekaj.api.enumtypes.AlertStatus;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 2/26/13
 * Time: 4:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlertStatusCassandraValidator extends AbstractType<AlertStatus> {

    public static final AlertStatusCassandraValidator instance = new AlertStatusCassandraValidator();

    @Override
    public AlertStatus compose(ByteBuffer byteBuffer) {
        return AlertStatus.fromValue(StringSerializer.get().fromByteBuffer(byteBuffer));
    }

    @Override
    public ByteBuffer decompose(AlertStatus alertStatus) {
        return StringSerializer.get().toByteBuffer(alertStatus.getStatusName());  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getString(ByteBuffer byteBuffer) {
        return StringSerializer.get().fromByteBuffer(byteBuffer);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ByteBuffer fromString(String s) throws MarshalException {
        return StringSerializer.get().toByteBuffer(s);  //To change body of implemented methods use File | Settings | File Templates.
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

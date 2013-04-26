package org.eurekaj.plugins.util;

import me.prettyprint.cassandra.serializers.StringSerializer;
import org.eurekaj.api.enumtypes.AlertStatus;
import org.firebrandocm.dao.TypeConverter;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 2/26/13
 * Time: 3:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlertStatusTypeConverter implements TypeConverter<AlertStatus> {

    @Override
    public AlertStatus fromValue(ByteBuffer byteBuffer) throws Exception {
        return AlertStatus.fromValue(StringSerializer.get().fromByteBuffer(byteBuffer));  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ByteBuffer toValue(AlertStatus alertStatus) throws Exception {
        return StringSerializer.get().toByteBuffer(alertStatus.getStatusName());  //To change body of implemented methods use File | Settings | File Templates.
    }
}

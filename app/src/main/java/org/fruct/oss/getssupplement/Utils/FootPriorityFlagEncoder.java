package org.fruct.oss.getssupplement.Utils;

import com.graphhopper.reader.OSMWay;
import com.graphhopper.routing.util.EncodedValue;
import com.graphhopper.routing.util.FootFlagEncoder;

import java.util.HashSet;
import java.util.Set;

public class FootPriorityFlagEncoder extends FootFlagEncoder {
    //private EncodedValue sidewalkEncodedValue;
    private EncodedValue priorityEncodedValue;
    private Set<String> streetPriorityHighways = new HashSet<String>();
    private Set<String> residentialPriorityHighways = new HashSet<String>();
    public FootPriorityFlagEncoder() {
        super(4, 1);
        streetPriorityHighways.add("primary");
        streetPriorityHighways.add("secondary");
        streetPriorityHighways.add("tertiary");
        streetPriorityHighways.add("trunk");
        streetPriorityHighways.add("motorway");
        streetPriorityHighways.add("unclassified");
        residentialPriorityHighways.add("residential");
        residentialPriorityHighways.add("service");
    }

    @Override
    public int defineWayBits(int index, int shift) {
        shift = super.defineWayBits(index, shift);
//sidewalkEncodedValue = new EncodedValue("sidewalk", shift, 2, 1, 3, 3, true);
//shift += sidewalkEncodedValue.getBits();
        priorityEncodedValue = new EncodedValue("priority", shift, 2, 1, 0, 3, true);
        shift += priorityEncodedValue.getBits();
        return shift;
    }
    @Override
    public long handleWayTags(OSMWay way, long allowed, long relationCode) {
        // if (!isAccept(allowed))
        //    return 0;

        long encoded = super.handleWayTags(way, allowed, relationCode);
        Priority priority;
        if (way.hasTag("highway", streetPriorityHighways)) {
            priority = Priority.STREET;
        } else if (way.hasTag("highway", residentialPriorityHighways)) {
            priority = Priority.RESIDENTIAL;
        } else {
            priority = Priority.FOOT;
        }
        String sidewalk = way.getTag("sidewalk");
        if (priority == Priority.STREET && sidewalk != null && !sidewalk.equals("no") && !sidewalk.equals("none")) {
            priority = Priority.STREET_WITH_SIDEWALK;
        }
        encoded = priorityEncodedValue.setValue(encoded, priority.ordinal());
        return encoded;
    }

    public Priority getPriority(long flags) {
        return Priority.values()[((int) priorityEncodedValue.getValue(flags))];
    }

    public static enum Priority {
        STREET(2),
        RESIDENTIAL(1.7),
        FOOT(0.6),
        STREET_WITH_SIDEWALK(0.9);
        private final double rate;
        private Priority(double v) {
            this.rate = v;
        }
        public double getRate() {
            return rate;
        }
    }

    @Override
    public String toString() {
        return "pfoot";
    }
}

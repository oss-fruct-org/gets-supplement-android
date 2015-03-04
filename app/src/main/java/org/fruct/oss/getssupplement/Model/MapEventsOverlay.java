package org.fruct.oss.getssupplement.Model;


import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;


import com.mapbox.mapboxsdk.overlay.MapEventsReceiver;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.util.Projection;


/**
 * Empty overlay than can be used to detect events on the map,
 * and to throw them to a MapEventsReceiver.
 * @author M.Kergall
 */
public class MapEventsOverlay extends Overlay {

    private MapEventsReceiver mReceiver;

    /**
     * @param ctx the context
     * @param receiver the object that will receive/handle the events.
     * It must implement MapEventsReceiver interface.
     */
    public MapEventsOverlay(Context ctx, MapEventsReceiver receiver) {
        super(ctx);
        mReceiver = receiver;
    }

    @Override protected void draw(Canvas c, MapView osmv, boolean shadow) {
        //Nothing to draw
    }

    @Override public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView){
        Projection proj = mapView.getProjection();
        //GeoPoint p = (GeoPoint)proj.fromPixels((int)e.getX(), (int)e.getY());
        return true;//mReceiver.singleTapConfirmedHelper(p);
    }

    @Override public boolean onLongPress(MotionEvent e, MapView mapView) {
        Projection proj = mapView.getProjection();
        //GeoPoint p = (GeoPoint)proj.fromPixels((int)e.getX(), (int)e.getY());
        //throw event to the receiver:
        return true;//mReceiver.longPressHelper(p);
    }

}

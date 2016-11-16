package amap.android_multiple_infowindows;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements PoiSearch.OnPoiSearchListener {
    private AMap aMap;
    private MapView mapView;
    private LatLonPoint centerpoint = new LatLonPoint(39.983178,116.464348);
    private ViewPoiOverlay poiOverlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
        //Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
        // MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
        doPOISearch();
    }

    private void doPOISearch() {
        PoiSearch.Query query = new PoiSearch.Query("公园","110101","北京");
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(0);
        query.requireSubPois(true);
        PoiSearch poiSearch = new PoiSearch(this,query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.setBound(new PoiSearch.SearchBound(centerpoint, 5000, true));//
        // 设置搜索区域为以lp点为圆心，其周围5000米范围
        poiSearch.searchPOIAsyn();// 异步搜索
    }

    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(convertToLatLng(centerpoint),13));
    }

//    private void addMarker(LatLng position, String title) {
//        if (position != null){
//            //初始化marker内容
//            MarkerOptions markerOptions = new MarkerOptions();
//            //这里很简单就加了一个TextView，根据需求可以加载复杂的View
//            View view = View.inflate(this, R.layout.custom_view, null);
//            TextView textView = ((TextView) view.findViewById(R.id.title));
//            textView.setText(title);
//            BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromView(view);
//            markerOptions.position(position).icon(markerIcon);
//            aMap.addMarker(markerOptions);
//        }
//    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    //搜索返回结果回调
    @Override
    public void onPoiSearched(PoiResult poiResult, int errorCode) {
        if (errorCode == 1000) {
            if (poiResult != null && poiResult.getQuery() != null) {

                List<PoiItem> poiItems = poiResult.getPois();
                if (poiItems != null && poiItems.size() > 0) {
                    aMap.clear();// 清理之前的图标
                    poiOverlay = new ViewPoiOverlay(aMap, poiItems);
                    poiOverlay.removeFromMap();
                    poiOverlay.addToMap();
                    poiOverlay.zoomToSpan();
                } else {
                    Toast.makeText(MainActivity.this, "无搜索结果", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "无搜索结果", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
    /**
     * 把LatLonPoint对象转化为LatLon对象
     */
    public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
        if (latLonPoint ==null){
            return null;
        }
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
    }

    public class ViewPoiOverlay extends PoiOverlay{

        public ViewPoiOverlay(AMap aMap, List<PoiItem> list) {
            super(aMap, list);
        }

        @Override
        protected BitmapDescriptor getBitmapDescriptor(int index) {
            View view = null;
            view = View.inflate(MainActivity.this, R.layout.custom_view, null);
            TextView textView = ((TextView) view.findViewById(R.id.title));
            textView.setText(getTitle(index));

            return  BitmapDescriptorFactory.fromView(view);
        }
    }
}

package net.osmand.plus.resources;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.osmand.PlatformUtil;
import net.osmand.ResultMatcher;
import net.osmand.binary.BinaryMapIndexReader;
import net.osmand.binary.BinaryMapIndexReader.SearchRequest;
import net.osmand.data.LatLon;
import net.osmand.data.TransportRoute;
import net.osmand.data.TransportStop;
import net.osmand.util.MapUtils;

import org.apache.commons.logging.Log;

public class TransportIndexRepositoryBinary implements TransportIndexRepository {
	private static final Log log = PlatformUtil.getLog(TransportIndexRepositoryBinary.class);
	private final BinaryMapIndexReader file;
	

	public TransportIndexRepositoryBinary(BinaryMapIndexReader file) {
		this.file = file;
	}

	@Override
	public boolean checkContains(double latitude, double longitude) {
		return file.containTransportData(latitude, longitude);
	}
	@Override
	public boolean checkContains(double topLatitude, double leftLongitude, double bottomLatitude, double rightLongitude) {
		return file.containTransportData(topLatitude, leftLongitude, bottomLatitude, rightLongitude);
	}
	
	@Override
	public void searchTransportStops(double topLatitude, double leftLongitude, double bottomLatitude, double rightLongitude,
			int limit, List<TransportStop> stops, ResultMatcher<TransportStop> matcher) {
		long now = System.currentTimeMillis();
		try {
			file.searchTransportIndex(BinaryMapIndexReader.buildSearchTransportRequest(MapUtils.get31TileNumberX(leftLongitude),
					MapUtils.get31TileNumberX(rightLongitude), MapUtils.get31TileNumberY(topLatitude), 
					MapUtils.get31TileNumberY(bottomLatitude), limit, stops));
			if (log.isDebugEnabled()) {
				log.debug(String.format("Search for %s done in %s ms found %s.", //$NON-NLS-1$
						topLatitude + " " + leftLongitude, System.currentTimeMillis() - now, stops.size())); //$NON-NLS-1$
			}
		} catch (IOException e) {
			log.error("Disk error ", e); //$NON-NLS-1$
		}
	}

	@Override
	public Collection<TransportRoute> getRouteForStop(TransportStop stop){
		try {
			Collection<TransportRoute> res = file.getTransportRoutes(stop.getReferencesToRoutes()).valueCollection();
			if(res != null){
				return res;
			}
		} catch (IOException e) {
			log.error("Disk error ", e); //$NON-NLS-1$
		}
		return Collections.emptyList();
	}
	

	@Override
	public List<RouteInfoLocation> searchTransportRouteStops(double latitude, double longitude, LatLon locationToGo, int zoom) {
		long now = System.currentTimeMillis();
		final LatLon loc = new LatLon(latitude, longitude);
		double tileNumberX = MapUtils.getTileNumberX(zoom, longitude);
		double tileNumberY = MapUtils.getTileNumberY(zoom, latitude);
		double topLatitude = MapUtils.getLatitudeFromTile(zoom, tileNumberY - 0.5);
		double bottomLatitude = MapUtils.getLatitudeFromTile(zoom, tileNumberY + 0.5);
		double leftLongitude = MapUtils.getLongitudeFromTile(zoom, tileNumberX - 0.5);
		double rightLongitude = MapUtils.getLongitudeFromTile(zoom, tileNumberX + 0.5);
		SearchRequest<TransportStop> req = BinaryMapIndexReader.buildSearchTransportRequest(MapUtils.get31TileNumberX(leftLongitude),
				MapUtils.get31TileNumberX(rightLongitude), MapUtils.get31TileNumberY(topLatitude), MapUtils
						.get31TileNumberY(bottomLatitude), -1, null);
		List<RouteInfoLocation> listRoutes = new ArrayList<RouteInfoLocation>();
		try {
			List<TransportStop> stops = file.searchTransportIndex(req);

			TIntObjectHashMap<TransportStop> stopsToProcess = new TIntObjectHashMap<TransportStop>();
			for (TransportStop s : stops) {
				for (int ref : s.getReferencesToRoutes()) {
					TransportStop exist = stopsToProcess.get(ref);
					if (exist == null || MapUtils.getDistance(loc, s.getLocation()) < MapUtils.getDistance(loc, exist.getLocation())) {
						stopsToProcess.put(ref, s);
					}
				}
			}
			TIntObjectHashMap<TransportRoute> transportRoutes = file.getTransportRoutes(stopsToProcess.keys());
			for (int ref : stopsToProcess.keys()) {
				TransportRoute route = transportRoutes.get(ref);
				TransportStop s = stopsToProcess.get(ref);
				for (int i = 0; i < 2; i++) {
					boolean direction = i == 0;
					List<TransportStop> stps = direction ? route.getForwardStops() : route.getBackwardStops();
					// load only part
					
					while (!stps.isEmpty() && (stps.get(0).getId().longValue() != s.getId().longValue())) {
						stps.remove(0);
					}
					if (!stps.isEmpty()) {
						RouteInfoLocation r = new RouteInfoLocation();
						r.setRoute(route);
						r.setStart(stps.get(0));
						r.setDirection(direction);
						if (locationToGo != null) {
							int distToLoc = Integer.MAX_VALUE;
							for (TransportStop st : stps) {
								double ndist = MapUtils.getDistance(locationToGo, st.getLocation());
								if (ndist < distToLoc) {
									distToLoc = (int) ndist;
									r.setStop(st);
									r.setDistToLocation(distToLoc);
								}
							}

						}
						listRoutes.add(r);
					}
				}
			}
			if (log.isDebugEnabled()) {
				log.debug(String.format("Search for routes done in %s ms found %s.", //$NON-NLS-1$
						System.currentTimeMillis() - now, listRoutes.size()));
			}

			if (locationToGo != null) {
				Collections.sort(listRoutes, new Comparator<RouteInfoLocation>() {
					@Override
					public int compare(RouteInfoLocation object1, RouteInfoLocation object2) {
						int x = (int) (MapUtils.getDistance(loc, object1.getStart().getLocation()) + object1.getDistToLocation());
						int y = (int) (MapUtils.getDistance(loc, object2.getStart().getLocation()) + object2.getDistToLocation());
						return x - y;
					}

				});
			} else {
				Collections.sort(listRoutes, new Comparator<RouteInfoLocation>() {
					@Override
					public int compare(RouteInfoLocation object1, RouteInfoLocation object2) {
						return Double.compare(MapUtils.getDistance(loc, object1.getStart().getLocation()), MapUtils.getDistance(loc,
								object2.getStart().getLocation()));
					}

				});
			}
		} catch (IOException e) {
			log.error("Disk error", e); //$NON-NLS-1$
		}
		return listRoutes;

	}


	@Override
	public boolean acceptTransportStop(TransportStop stop) {
		return file.transportStopBelongsTo(stop);
	}

	@Override
	public void close() {
		try {
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

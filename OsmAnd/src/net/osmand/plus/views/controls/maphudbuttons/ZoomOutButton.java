package net.osmand.plus.views.controls.maphudbuttons;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.views.layers.MapControlsLayer;

public class ZoomOutButton extends MapButton {

	public ZoomOutButton(@NonNull MapActivity mapActivity, @NonNull ImageView view, @NonNull String id) {
		this(mapActivity, view, id, false);
	}

	public ZoomOutButton(@NonNull MapActivity mapActivity, @NonNull ImageView view, @NonNull String id, boolean alwaysVisible) {
		super(mapActivity, view, id, alwaysVisible);
		setIconId(R.drawable.ic_zoom_out);
		setRoundTransparentBackground();
		setOnClickListener(v -> {
			if (mapActivity.getContextMenu().zoomOutPressed()) {
				return;
			}
			mapActivity.getMapView().zoomOut();
		});
		setOnLongClickListener(MapControlsLayer.getOnClickMagnifierListener(mapActivity.getMapView()));
		updateIcon(app.getDaynightHelper().isNightModeForMapControls());
	}

	@Override
	protected boolean shouldShow() {
		return alwaysVisible || !isRouteDialogOpened() && visibilityHelper.shouldShowZoomButtons();
	}
}
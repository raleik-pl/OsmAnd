package net.osmand.plus.views.controls.maphudbuttons;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.views.layers.MapControlsLayer;

public class ZoomInButton extends MapButton {

	public ZoomInButton(@NonNull MapActivity mapActivity, @NonNull ImageView view, @NonNull String id) {
		this(mapActivity, view, id, false);
	}

	public ZoomInButton(@NonNull MapActivity mapActivity, @NonNull ImageView view, @NonNull String id, boolean alwaysVisible) {
		super(mapActivity, view, id, alwaysVisible);
		setIconId(R.drawable.ic_zoom_in);
		setRoundTransparentBackground();
		setOnClickListener(v -> {
			if (mapActivity.getContextMenu().zoomInPressed()) {
				return;
			}
			mapActivity.getMapView().zoomIn();
		});
		setOnLongClickListener(MapControlsLayer.getOnClickMagnifierListener(mapActivity.getMapView()));
		updateIcon(app.getDaynightHelper().isNightModeForMapControls());
	}

	@Override
	protected boolean shouldShow() {
		return alwaysVisible || !isRouteDialogOpened() && visibilityHelper.shouldShowZoomButtons();
	}
}
package net.osmand.plus.myplaces.tracks.filters;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

public interface CollectionTrackFilterParams {
	default boolean hasSelectAllVariant() {
		return false;
	}
	default String getItemText(String itemName){
		return itemName;
	}

	@Nullable
	default Drawable getItemIcon(String itemName )  {
		return null;
	}

	@Nullable
	default Drawable getSelectAllItemIcon(boolean isChecked, boolean nightMode) {
		return null;
	}

}

package net.osmand.plus.download.local;

import androidx.annotation.NonNull;

public abstract class LocalItem {

	private final LocalItemType type;

	public LocalItem(@NonNull LocalItemType type) {
		this.type = type;
	}

	@NonNull
	public LocalItemType getType() {
		return type;
	}
}
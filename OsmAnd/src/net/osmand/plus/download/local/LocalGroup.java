package net.osmand.plus.download.local;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class LocalGroup {

	private final LocalItemType type;
	private final List<LocalFileItem> items = new ArrayList<>();

	public LocalGroup(@NonNull LocalItemType type) {
		this.type = type;
	}

	@NonNull
	public LocalItemType getType() {
		return type;
	}

	@NonNull
	public List<LocalFileItem> getItems() {
		return items;
	}

	@NonNull
	public String getName(@NonNull Context context) {
		return type.toHumanString(context);
	}

	public void addItem(@NonNull LocalFileItem localItem) {
		items.add(localItem);
	}

	public void removeItem(@NonNull LocalFileItem localItem) {
		items.remove(localItem);
	}

	public long getSize() {
		long size = 0;
		for (LocalFileItem item : items) {
			size += item.getSize();
		}
		return size;
	}
}

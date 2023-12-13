package net.osmand.plus.download.local;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalGroupItem extends LocalItem {

	private List<File> files = new ArrayList<>();

	public LocalGroupItem(@NonNull LocalItemType type) {
		super(type);
	}
}

package net.osmand.plus.download.ui;

import net.osmand.plus.download.local.LocalFileItem;

public interface AbstractLoadLocalIndexTask {
	void loadFile(LocalFileItem... loaded);
}

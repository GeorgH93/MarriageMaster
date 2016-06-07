/*
 *   Copyright (C) 2016 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Updater;

import at.pcgamingfreaks.MarriageMaster.Updater.UpdateProviders.NotSuccessfullyQueriedException;
import at.pcgamingfreaks.MarriageMaster.Updater.UpdateProviders.UpdateProvider;

import java.io.*;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * This is a very raw implementation of a plugin updater only using java functions.
 * This way we can use the same code on Bukkit/Spigot and BungeeCord
 */
public abstract class Updater
{
	private final static int BUFFER_SIZE = 1024;

	private final File pluginsFolder, updateFolder;
	private final UpdateProvider updateProvider;
	private final boolean announceDownloadProgress;
	private final Logger logger;
	private final String localVersion, targetFileName;

	private UpdateResult result;

	protected Updater(File pluginsFolder, boolean announceDownloadProgress, Logger logger, UpdateProvider updateProvider, String localVersion, String targetFileName)
	{
		this(pluginsFolder, new File(pluginsFolder, "updates"), announceDownloadProgress, logger, updateProvider, localVersion, targetFileName);
	}

	protected Updater(File pluginsFolder, File updateFolder, boolean announceDownloadProgress, Logger logger, UpdateProvider updateProvider, String localVersion, String targetFileName)
	{
		this.pluginsFolder = pluginsFolder;
		this.updateFolder = updateFolder;
		this.updateProvider = updateProvider;
		this.announceDownloadProgress = announceDownloadProgress;
		this.logger = logger;
		this.localVersion = localVersion;
		this.targetFileName = targetFileName;
	}

	/**
	 * Used to sync back after async update or update check.
	 *
	 * @param runnable The runnable that will be synced.
	 */
	protected abstract void runSync(Runnable runnable);

	/**
	 * Used to run the update or check functions async.
	 *
	 * @param runnable The runnable that should run async.
	 */
	protected abstract void runAsync(Runnable runnable);

	/**
	 * Gets the plugin author for the info output.
	 *
	 * @return The plugin author.
	 */
	protected abstract String getAuthor();

	/**
	 * Waits for the async worker to finish.
	 * We need to prevent the server from closing while we still work.
	 */
	public abstract void waitForAsyncOperation();

	//region version checking logic

	/**
	 * This function prepares a given version string to be interpreted by the updater.
	 * The version therefor will be split on each "." to get the individual parts of the version string.
	 * If it's a snapshot/alpha/beta build we reduce her last digit by 1 so that the updater will kick in as soon as the final of the version is released.
	 *
	 * @param version The version to prepare for interpretation
	 * @return The prepared version
	 */
	protected String[] prepareVersion(String version)
	{
		String[] v = version.toLowerCase().split("-")[0].split(Pattern.quote("."));
		try
		{
			if(version.contains("snapshot") || version.contains("alpha") || version.contains("beta"))
			{
				if(v.length == 1)
				{
					v = new String[] { Integer.toString(Integer.parseInt(v[0]) - 1), Integer.toString(Integer.MAX_VALUE) };
				}
				else
				{
					for(int i = v.length - 1; i > 0; i--)
					{
						if(Integer.parseInt(v[i]) > 0)
						{
							v[i] = Integer.toString(Integer.parseInt(v[i]) - 1);
							break;
						}
					}
				}
			}
		}
		catch(Exception ignored) {}
		return v;
	}

	/**
	 * This method provides a basic version comparison. If you don't like it's behavior please Override it in!
	 * <p> With default behavior, the Updater only supports this format: <b>\d(.\d)*(-SOMETHING)*</b>
	 * If the version string doesn't match this scheme the fallback of comparing local and remote version will be used.</p>
	 *
	 * @param remoteVersion the remote version
	 * @return true if the updater should consider the remote version an update, false if not.
	 */
	protected boolean shouldUpdate(String remoteVersion)
	{
		String[] locVersion = prepareVersion(localVersion), remVersion = prepareVersion(remoteVersion);
		try
		{
			int c = Math.min(locVersion.length, remVersion.length);
			for(int i = 0; i < c; i++)
			{
				int r = Integer.parseInt(remVersion[i]), l = Integer.parseInt(locVersion[i]);
				if(r > l)
				{
					return true;
				}
				else if(r < l)
				{
					return false;
				}
			}
			// If both version are the same for the length of the shorter version the version that has more digits probably is the newer one.
			if(remVersion.length > locVersion.length)
			{
				return true;
			}
		}
		catch(Exception e)
		{
			// There was a problem parsing the version. Use the fallback (if they don't match the remote version is the newer one)
			logger.warning("Failed to determine the newer version between local version \"" + localVersion +
					"\" and remote version \"" + remoteVersion + "\"! Using fallback method (if they don't match the remote version is the newer one)!");
			return !localVersion.equalsIgnoreCase(remoteVersion);
		}
		return false;
	}

	/**
	 * Check to see if the program should continue by evaluating whether the plugin is already updated, or shouldn't be updated.
	 *
	 * @param remoteVersion The version to compare against
	 * @return true if the version was located and is newer then the local version
	 */
	protected boolean versionCheck(String remoteVersion)
	{
		if(remoteVersion != null)
		{
			if(!this.shouldUpdate(remoteVersion))
			{
				// We already have the latest version, or this build is tagged for no-update
				result = UpdateResult.NO_UPDATE;
				return false;
			}
		}
		else
		{
			// OMG!!! We have no version to work with!
			logger.warning("There was a problem retrieving the remote version of the plugin!");
			logger.warning("You should contact the plugin author (" + getAuthor() + ") about this!");
			result = UpdateResult.FAIL_NO_VERSION_FOUND;
			return false;
		}
		return true;
	}
	//endregion

	protected void download(URL url, String fileName) // Saves file into servers update directory
	{
		if(!updateFolder.exists())
		{
			//noinspection ResultOfMethodCallIgnored
			updateFolder.mkdirs();
		}
		try
		{
			int fileLength = url.openConnection().getContentLength(), count, percent, percentHelper = -1;
			File downloadFile = new File(updateFolder.getAbsolutePath() + File.separator + fileName);
			MessageDigest md5HashGenerator = updateProvider.provideMD5Checksum() ? MessageDigest.getInstance("MD5") : null;
			try(InputStream inputStream = (md5HashGenerator != null) ? new DigestInputStream(new BufferedInputStream(url.openStream()), md5HashGenerator) : new BufferedInputStream(url.openStream());
			    FileOutputStream outputStream = new FileOutputStream(downloadFile))
			{
				byte[] buffer = new byte[BUFFER_SIZE];
				if(announceDownloadProgress)
				{
					logger.info("Start downloading update: " + updateProvider.getLatestVersion());
				}
				long downloaded = 0;
				while((count = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1)
				{
					downloaded += count;
					outputStream.write(buffer, 0, count);
					percent = (int) ((downloaded * 100) / fileLength);
					if(announceDownloadProgress && percent % 10 == 0 && percent / 10 > percentHelper)
					{
						percentHelper++;
						logger.info("Downloading update: " + percent + "% of " + fileLength + " bytes.");
					}
				}
				outputStream.flush();
			}
			if(md5HashGenerator != null)
			{
				String MD5Download = byteArrayToHex(md5HashGenerator.digest()).toLowerCase(), MD5Target = updateProvider.getLatestChecksum().toLowerCase();
				if(!MD5Download.equals(MD5Target))
				{
					logger.warning("The auto-updater was able to download the file, but the checksum did not match! Delete file.");
					logger.warning("Checksum expected: " + MD5Target + " Checksum download: " + MD5Download);
					result = UpdateResult.FAIL_DOWNLOAD;
					//noinspection ResultOfMethodCallIgnored
					downloadFile.delete();
					return;
				}
			}
			if(result != UpdateResult.FAIL_DOWNLOAD && announceDownloadProgress)
			{
				result = UpdateResult.SUCCESS;
				logger.info("Finished updating.");
			}
		}
		catch(Exception ignored)
		{
			logger.warning("The auto-updater tried to download a new update, but was unsuccessful.");
			ignored.printStackTrace();
			result = UpdateResult.FAIL_DOWNLOAD;
		}
	}

	private static String byteArrayToHex(byte[] bytes)
	{
		if(bytes == null || bytes.length == 0) return "";
		StringBuilder hexBuilder = new StringBuilder(bytes.length * 2);
		for(byte b: bytes)
		{
			hexBuilder.append(String.format("%02x", b));
		}
		return hexBuilder.toString();
	}

	/**
	 * Gets the latest remote version of the last query.
	 *
	 * @return The latest remote version of the last query. Null if there wasn't a successful query before.
	 */
	protected String getRemoteVersion()
	{
		try
		{
			return updateProvider.getLatestVersion();
		}
		catch(NotSuccessfullyQueriedException ignored) {}
		return null;
	}

	public void update()
	{
		update(null);
	}

	public void update(final UpdaterResponse response)
	{
		if(result == UpdateResult.DISABLED) return;
		runAsync(new Runnable()
		{
			@Override
			public void run()
			{
				result = updateProvider.query(logger);
				if(result == UpdateResult.SUCCESS)
				{
					if(getRemoteVersion().startsWith("2"))
					{
						result = UpdateResult.UPDATE_AVAILABLE_V2;
						if(response != null) runSync(new Runnable()
						{
							@Override
							public void run()
							{
								response.onDone(result);
							}
						});
					}
					else if(versionCheck(getRemoteVersion()))
					{
						result = UpdateResult.UPDATE_AVAILABLE;
						try
						{
							if(updateProvider.provideDownloadURL() && updateProvider.getLatestFileURL() != null)
							{
								download(updateProvider.getLatestFileURL(), (updateProvider.getLatestVersionFileName().toLowerCase().endsWith(".zip")) ? updateProvider.getLatestVersionFileName() : targetFileName);
								if(response != null) runSync(new Runnable()
								{
									@Override
									public void run()
									{
										response.onDone(result);
									}
								});
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		});
	}

	public interface UpdaterResponse
	{
		void onDone(UpdateResult result);
	}
}
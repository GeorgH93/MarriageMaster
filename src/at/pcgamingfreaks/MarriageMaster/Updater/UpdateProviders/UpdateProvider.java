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

package at.pcgamingfreaks.MarriageMaster.Updater.UpdateProviders;

import at.pcgamingfreaks.MarriageMaster.Updater.UpdateResult;

import java.net.URL;
import java.util.logging.Logger;

public interface UpdateProvider
{
	/**
	 * Make a connection to the provider an requests the file's details.
	 *
	 * @return The update result from the query.
	 */
	UpdateResult query(Logger logger);

	//region getter for the latest version
	/**
	 * Gets the latest version's version name (such as "1.32")
	 *
	 * @return The latest version string.
	 * @throws NotSuccessfullyQueriedException  If the provider has not been queried successfully before
	 */
	String getLatestVersion() throws NotSuccessfullyQueriedException;

	/**
	 * Get the latest version's direct download url.
	 *
	 * @return latest version's file download url.
	 * @throws at.pcgamingfreaks.Updater.UpdateProviders.RequestTypeNotAvailableException If the provider doesn't support the request type
	 * @throws NotSuccessfullyQueriedException  If the provider has not been queried successfully before
	 */
	URL getLatestFileURL() throws at.pcgamingfreaks.Updater.UpdateProviders.RequestTypeNotAvailableException, NotSuccessfullyQueriedException;

	/**
	 * Get the latest version's file name.
	 *
	 * @return latest version's file name.
	 * @throws at.pcgamingfreaks.Updater.UpdateProviders.RequestTypeNotAvailableException If the provider doesn't support the request type
	 * @throws NotSuccessfullyQueriedException  If the provider has not been queried successfully before
	 */
	String getLatestVersionFileName() throws at.pcgamingfreaks.Updater.UpdateProviders.RequestTypeNotAvailableException, NotSuccessfullyQueriedException;

	/**
	 * Get the latest version's name (such as "Project v1.0").
	 *
	 * @return latest version's name.
	 * @throws at.pcgamingfreaks.Updater.UpdateProviders.RequestTypeNotAvailableException If the provider doesn't support the request type
	 * @throws NotSuccessfullyQueriedException  If the provider has not been queried successfully before
	 */
	String getLatestName() throws at.pcgamingfreaks.Updater.UpdateProviders.RequestTypeNotAvailableException, NotSuccessfullyQueriedException;

	/**
	 * Get the latest version's checksum (md5).
	 *
	 * @return latest version's MD5 checksum.
	 * @throws at.pcgamingfreaks.Updater.UpdateProviders.RequestTypeNotAvailableException If the provider doesn't support the request type
	 * @throws NotSuccessfullyQueriedException  If the provider has not been queried successfully before
	 */
	String getLatestChecksum() throws at.pcgamingfreaks.Updater.UpdateProviders.RequestTypeNotAvailableException, NotSuccessfullyQueriedException;
	//endregion

	//region provider property's
	boolean provideDownloadURL();

	boolean provideMD5Checksum();
	//endregion
}
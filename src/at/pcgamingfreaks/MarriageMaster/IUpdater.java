/*
 *   Copyright (C) 2019 GeorgH93
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

package at.pcgamingfreaks.MarriageMaster;

import at.pcgamingfreaks.Updater.UpdateProviders.BukkitUpdateProvider;
import at.pcgamingfreaks.Updater.UpdateProviders.JenkinsUpdateProvider;
import at.pcgamingfreaks.Updater.UpdateProviders.UpdateProvider;
import at.pcgamingfreaks.Updater.Updater;

import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public interface IUpdater
{
	int BUKKIT_PROJECT_ID = 74734;
	String JENKINS_URL = "https://ci.pcgamingfreaks.at", JENKINS_JOB_DEV = "MarriageMaster V2", JENKINS_JOB_MASTER = "MarriageMaster";

	Updater createUpdater(UpdateProvider updateProvider);

	boolean isRelease();

	Logger getLogger();

	default Updater update(final @Nullable at.pcgamingfreaks.Updater.Updater.UpdaterResponse response)
	{
		UpdateProvider updateProvider;
		if(isRelease())
		{
			updateProvider = new BukkitUpdateProvider(BUKKIT_PROJECT_ID, getLogger());
		}
		else
		{
			/*if[STANDALONE]
			updateProvider = new JenkinsUpdateProvider(JENKINS_URL, JENKINS_JOB_MASTER, getLogger(), ".*-Standalone.*");
			else[STANDALONE]*/
			updateProvider = new JenkinsUpdateProvider(JENKINS_URL, JENKINS_JOB_DEV, getLogger());
			/*end[STANDALONE]*/
		}
		Updater updater = createUpdater(updateProvider);
		updater.update(response);
		return updater;
	}
}
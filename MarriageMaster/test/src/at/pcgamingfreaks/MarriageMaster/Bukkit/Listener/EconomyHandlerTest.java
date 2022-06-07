/*
 *   Copyright (C) 2022 GeorgH93
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
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Listener;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Events.*;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Config;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Database.Language;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.Reflection;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class EconomyHandlerTest
{ // TODO: validate that the right message is sent
	private static MarriageMaster marriageMaster;
	private static PluginManager pluginManager;
	private static RegisteredServiceProvider<Economy> serviceProvider;
	private static MarriagePlayer marriagePlayer1, marriagePlayer2;
	private static Player player1, player2, player3;
	private static final HashMap<OfflinePlayer, Double> money = new HashMap<>();
	private static Economy economy;
	private static Marriage marriage;

	@Mock
	private Plugin vaultPlugin;
	@Mock
	private PluginLogger logger;
	private EconomyHandler economyHandler;

	@SuppressWarnings("RedundantCast")
	@BeforeAll
	static void setUpAll()
	{
		marriageMaster = mock(MarriageMaster.class);
		Language language = mock(Language.class);
		Config configuration = mock(Config.class);
		when(language.getMessage(anyString())).thenAnswer((Answer<Message>) invocationOnMock -> {
			Message mockedMessage = mock(Message.class);
			when(mockedMessage.placeholder(anyString(), any())).thenReturn(mockedMessage);
			return mockedMessage;
		});
		when(configuration.getEconomyValue(anyString())).thenReturn(10.0);
		when(marriageMaster.getConfiguration()).thenReturn(configuration);
		when(marriageMaster.getLanguage()).thenReturn(language);
		Server server = mock(Server.class);
		when(marriageMaster.getServer()).thenReturn(server);
		pluginManager = mock(PluginManager.class);
		when(server.getPluginManager()).thenReturn(pluginManager);
		ServicesManager servicesManager = mock(ServicesManager.class);
		when(server.getServicesManager()).thenReturn(servicesManager);
		serviceProvider = (RegisteredServiceProvider<Economy>) mock(RegisteredServiceProvider.class);
		when(servicesManager.getRegistration(Economy.class)).thenReturn(serviceProvider);
		marriagePlayer1 = mock(MarriagePlayer.class);
		marriagePlayer2 = mock(MarriagePlayer.class);
		player1 = mock(Player.class);
		player2 = mock(Player.class);
		player3 = mock(Player.class);
		when(marriagePlayer1.getPlayer()).thenReturn(player1);
		when(marriagePlayer2.getPlayer()).thenReturn(player2);
		economy = mock(Economy.class);
		when(economy.has(any(OfflinePlayer.class), any(double.class))).thenAnswer((Answer<Boolean>) invocation -> money.get((OfflinePlayer) invocation.getArgument(0)) >= (double)invocation.getArgument(1));
		when(economy.withdrawPlayer(any(OfflinePlayer.class), any(double.class))).thenAnswer((Answer<EconomyResponse>) invocation -> {
			boolean success = money.get((OfflinePlayer)invocation.getArgument(0)) >= (double)invocation.getArgument(1);
			if(success) money.put(invocation.getArgument(0), money.get((OfflinePlayer)invocation.getArgument(0)) - (double)invocation.getArgument(1));
			return new EconomyResponse((double)invocation.getArgument(1), money.get((OfflinePlayer)invocation.getArgument(0)), success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, "");
		});
		marriage = mock(Marriage.class);
		when(marriage.getPartner1()).thenReturn(marriagePlayer1);
		when(marriage.getPartner2()).thenReturn(marriagePlayer2);
	}

	@BeforeEach
	void setUp()
	{
		when(pluginManager.getPlugin("Vault")).thenReturn(vaultPlugin);
		when(serviceProvider.getProvider()).thenReturn(economy);
		when(marriageMaster.getLogger()).thenReturn(logger);
		economyHandler = new EconomyHandler(marriageMaster);
		money.clear();
		money.put(player1, 11.5);
		money.put(player2, 11.5);
		money.put(player3, 11.5);
	}

	@Test
	void testSetupEconomy() throws InvocationTargetException, IllegalAccessException
	{
		Method setupEconomyMethod = Reflection.getMethod(EconomyHandler.class, "setupEconomy", MarriageMaster.class);
		assertTrue((boolean) setupEconomyMethod.invoke(economyHandler, marriageMaster));
		when(pluginManager.getPlugin("Vault")).thenReturn(null);
		assertFalse((boolean) setupEconomyMethod.invoke(economyHandler, marriageMaster));
		when(pluginManager.getPlugin("Vault")).thenReturn(vaultPlugin);
		when(serviceProvider.getProvider()).thenReturn(null);
		assertFalse((boolean) setupEconomyMethod.invoke(economyHandler, marriageMaster));
	}

	@Test
	void testOnMarry() throws Exception
	{
		Reflection.setValue(economyHandler, "costMarry", 10.0);
		MarryEvent event = new MarryEvent(marriagePlayer1, marriagePlayer2, player3, null);
		economyHandler.onMarry(event);
		assertFalse(event.isCancelled());
		event = new MarryEvent(marriagePlayer1, marriagePlayer2, player3, null);
		economyHandler.onMarry(event);
		assertTrue(event.isCancelled());
		money.put(player1, 11.5);
		event = new MarryEvent(marriagePlayer1, marriagePlayer2, player3, null);
		economyHandler.onMarry(event);
		assertTrue(event.isCancelled());
		assertEquals(11.5, (double) money.get(player1));
		money.put(player1, 1.5);
		money.put(player2, 11.5);
		event = new MarryEvent(marriagePlayer1, marriagePlayer2, player3, null);
		economyHandler.onMarry(event);
		assertTrue(event.isCancelled());
		assertEquals(11.5, (double) money.get(player2));
		Reflection.setValue(economyHandler, "costMarry", -1.0);
		event = new MarryEvent(marriagePlayer1, marriagePlayer2, player3, null);
		economyHandler.onMarry(event);
		assertFalse(event.isCancelled());
	}

	@Test
	void testOnDivorce()
	{
		Reflection.setValue(economyHandler, "costDivorce", 10.0);
		DivorceEvent event = new DivorceEvent(marriage, player3);
		economyHandler.onDivorce(event);
		assertFalse(event.isCancelled());
		event = new DivorceEvent(marriage, player3);
		economyHandler.onDivorce(event);
		assertTrue(event.isCancelled());
		money.put(player1, 11.5);
		event = new DivorceEvent(marriage, player3);
		economyHandler.onDivorce(event);
		assertTrue(event.isCancelled());
		assertEquals(11.5, (double) money.get(player1));
		money.put(player1, 1.5);
		money.put(player2, 11.5);
		event = new DivorceEvent(marriage, player3);
		economyHandler.onDivorce(event);
		assertTrue(event.isCancelled());
		assertEquals(11.5, (double) money.get(player2));
		Reflection.setValue(economyHandler, "costDivorce", -1.0);
		event = new DivorceEvent(marriage, player3);
		economyHandler.onDivorce(event);
		assertFalse(event.isCancelled());
	}

	@Test
	void testOnTeleport()
	{
		TPEvent event = new TPEvent(marriagePlayer1, marriage);
		economyHandler.onTeleport(event);
		assertFalse(event.isCancelled());
		event = new TPEvent(marriagePlayer1, marriage);
		economyHandler.onTeleport(event);
		assertTrue(event.isCancelled());
		Reflection.setValue(economyHandler, "costTp", -1.0);
		event = new TPEvent(marriagePlayer1, marriage);
		economyHandler.onTeleport(event);
		assertFalse(event.isCancelled());
	}

	@Test
	void testOnGift()
	{
		GiftEvent event = new GiftEvent(marriagePlayer1, marriage, mock(ItemStack.class));
		economyHandler.onGift(event);
		assertFalse(event.isCancelled());
		event = new GiftEvent(marriagePlayer1, marriage, mock(ItemStack.class));
		economyHandler.onGift(event);
		assertTrue(event.isCancelled());
		Reflection.setValue(economyHandler, "costGift", -1.0);
		event = new GiftEvent(marriagePlayer1, marriage, mock(ItemStack.class));
		economyHandler.onGift(event);
		assertFalse(event.isCancelled());
	}

	@Test
	void testOnHome()
	{
		HomeTPEvent event = new HomeTPEvent(marriagePlayer1, marriage);
		economyHandler.onHome(event);
		assertFalse(event.isCancelled());
		event = new HomeTPEvent(marriagePlayer1, marriage);
		economyHandler.onHome(event);
		assertTrue(event.isCancelled());
		Reflection.setValue(economyHandler, "costHome", -1.0);
		event = new HomeTPEvent(marriagePlayer1, marriage);
		economyHandler.onHome(event);
		assertFalse(event.isCancelled());
	}

	@Test
	void testOnSetHome()
	{
		HomeSetEvent event = new HomeSetEvent(marriagePlayer1, marriage, mock(Location.class));
		economyHandler.onSetHome(event);
		assertFalse(event.isCancelled());
		event = new HomeSetEvent(marriagePlayer1, marriage, mock(Location.class));
		economyHandler.onSetHome(event);
		assertTrue(event.isCancelled());
		Reflection.setValue(economyHandler, "costSetHome", -1.0);
		event = new HomeSetEvent(marriagePlayer1, marriage, mock(Location.class));
		economyHandler.onSetHome(event);
		assertFalse(event.isCancelled());
	}
}
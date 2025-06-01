package at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer;

import at.pcgamingfreaks.Bukkit.Placeholder.PlaceholderName;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.Marriage;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;

@PlaceholderName(aliases = "Married_Time")
public class MarriedTime extends PlaceholderReplacerBaseValue
{
    protected final String formatYears, formatMonths, formatDays, formatHours, formatMinutes, formatMain;

    public MarriedTime(MarriageMaster plugin)
    {
        super(plugin);
        formatYears = getPlaceholderValue("Years");
        formatMonths = getPlaceholderValue("Months");
        formatDays = getPlaceholderValue("Days");
        formatHours = getPlaceholderValue("Hours");
        formatMinutes = getPlaceholderValue("Minutes");
        formatMain = getPlaceholderValue("TimeMain");
    }

    @Override
    protected @Nullable String replaceMarried(MarriagePlayer player)
    {
        Marriage marriageData = player.getMarriageData();
        if(marriageData == null)
                return null;
        LocalDateTime start = LocalDateTime.ofInstant(marriageData.getWeddingDate().toInstant(), ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();

        Period period = Period.between(start.toLocalDate(), now.toLocalDate());
        long years = period.getYears();
        long months = period.getMonths();
        long days = period.getDays();

        LocalDateTime tmp = start.plusYears(years).plusMonths(months).plusDays(days);
        Duration duration = Duration.between(tmp, now);
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();

        String yearsStr = years > 0 && formatYears != null ? formatYears.replace("{years}", Long.toString(years)) : "";
        String monthsStr = months > 0 && formatMonths != null ? formatMonths.replace("{months}", Long.toString(months)) : "";
        String daysStr = days > 0 && formatDays != null ? formatDays.replace("{days}", Long.toString(days)) : "";
        String hoursStr = hours > 0 && formatHours != null ? formatHours.replace("{hours}", Long.toString(hours)) : "";
        String minutesStr = minutes > 0 && formatMinutes != null ? formatMinutes.replace("{minutes}", Long.toString(minutes)) : "";

        String result = (formatMain != null ? formatMain : "{years}{months}{days}{hours}{minutes}");
        result = result.replace("{years}", yearsStr);
        result = result.replace("{months}", monthsStr);
        result = result.replace("{days}", daysStr);
        result = result.replace("{hours}", hoursStr);
        result = result.replace("{minutes}", minutesStr);
        return result;
    }

    @Override
    public @Nullable String getFormat()
    {
        return formatMain;
    }
}

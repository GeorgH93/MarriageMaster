package at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer.Formatted;

import at.pcgamingfreaks.Bukkit.Placeholder.PlaceholderName;
import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriagePlayer;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Placeholder.Replacer.MarriedTime;

import org.jetbrains.annotations.Nullable;

@PlaceholderName(name = "MarriedTime", aliases = "Married_Time")
public class MarriedTimeFormatted extends MarriedTime
{
    public MarriedTimeFormatted(MarriageMaster plugin)
    {
        super(plugin);
    }

    @Override
    protected @Nullable String replaceMarried(MarriagePlayer player)
    {
        return String.format(formatMain, super.replaceMarried(player));
    }
}

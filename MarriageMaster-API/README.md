<!-- Variables (this block will not be visible in the readme -->
[banner]: https://pcgamingfreaks.at/images/marriagemaster.png
[spigot]: https://www.spigotmc.org/resources/19273/
[license]: https://github.com/GeorgH93/MarriageMaster/blob/master/LICENSE
[licenseImg]: https://img.shields.io/github/license/GeorgH93/MarriageMaster.svg
[ci]: https://ci.pcgamingfreaks.at/job/MarriageMaster%20API/
[ciImg]: https://ci.pcgamingfreaks.at/job/MarriageMaster%20API/badge/icon
[apiVersionImg]: https://img.shields.io/badge/dynamic/xml.svg?label=api-version&query=%2F%2Frelease[1]&url=https%3A%2F%2Frepo.pcgamingfreaks.at%2Frepository%2Fmaven-releases%2Fat%2Fpcgamingfreaks%2FMarriageMaster-API%2Fmaven-metadata.xml
[apiJavaDoc]: https://ci.pcgamingfreaks.at/job/MarriageMaster%20API/javadoc/
[apiBuilds]: https://ci.pcgamingfreaks.at/job/MarriageMaster%20API/
<!-- End of variables block -->

[![Logo][banner]][spigot]

This folder contains the API for the MarriageMaster plugin.

[![ciImg]][ci] [![apiVersionImg]][apiJavaDoc] [![licenseImg]][license]

## Adding it to your plugin:
### Maven:
The API is available through maven.
#### Repository:
```
<repository>
	<id>pcgf-repo</id>
	<url>https://repo.pcgamingfreaks.at/repository/everything</url>
</repository>
```
#### Dependency:
```
<!-- Marriage Master API -->
<dependency>
    <groupId>at.pcgamingfreaks</groupId>
    <artifactId>MarriageMaster-API</artifactId>
    <version>2.1</version><!-- Check api-version shield for newest version -->
</dependency>
```

### Build from source:
```
git clone https://github.com/GeorgH93/MarriageMaster.git
cd MarriageMaster
mvn -pl MarriageMaster-API
```

### Get access to the API:
```java
public MarriageMasterPlugin getMarriageMaster() {
    Plugin bukkitPlugin = Bukkit.getPluginManager().getPlugin("MarriageMaster");
    if(!(bukkitPlugin instanceof MarriageMasterPlugin)) {
    	// Do something if MarriageMaster is not available
        return null;
    }
    return (MarriageMasterPlugin) bukkitPlugin;
}
```
You can now use the returned `MarriageMasterPlugin` object to interact with the MarriageMaster plugin.

## Links:
* [JavaDoc][apiJavaDoc]
* [API Build Server][apiBuilds]

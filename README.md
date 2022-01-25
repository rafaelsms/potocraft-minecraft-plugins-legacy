# PotoCraft Plugins

Custom plugins for my Minecraft server, [PotoCraft](http://potocraft.com).

## Building

For each module, you must do:

```sh
$ ./gradlew shadowJar
```

And the plugin jar will be available on module's `build/lib/` folder with the suffix `-all.jar`.

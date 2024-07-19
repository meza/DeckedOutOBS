# DeckedOutOBS (Fabric)

DeckedOutOBS is a client-side Minecraft mod that enhances the Decked Out 2 game that was built by 
[TangoTek](https://www.youtube.com/@TangoTekLP) on the [Hermitcraft](https://hermitcraft.com/) Season 9 server. 
This mod provides card callouts directly into an OBS Browser Source, allowing your audience 
to see the cards being played while you're running the dungeon.

![](/docs/example.gif)

## Features

- **Client-Side Only**: No need to install the mod on servers.
- **Card Callouts**: Displays the cards being played in real-time through an OBS Browser Source.
- **Flexible**: You can resize and move the browser source card as you wish.
- **Easy Calibration**: Simple setup with in-game commands, no permissions needed.
- **Customizable Port**: Change the server port number if needed.

## Installation

1. Download the [DeckedOutOBS mod](https://modrinth.com/mod/deckedoutobs) and place it into your Minecraft `mods` folder.
2. If you don't have it yet, download the [Fabric API mod](https://modrinth.com/mod/fabric-api) and place it into your Minecraft `mods` folder.
3. Start your Minecraft client with the mod installed.

## Setup

### Calibration

To calibrate the mod, you need to set the key barrel position where you place the frozen shards to start Decked Out 2. 
Use the following command in-game:

```plaintext
/deckedout dungeon set <key barrel position>
```

Replace `<key barrel position>` with the x, y, z coordinates of the barrel.
If you're looking at the barrel, Minecraft should automatically suggest the correct coordinates

![Calibrating](/docs/calibrating.png)

### Starting the Server

Once the dungeon is set, the mod will start the server automatically. 
The port number and the Browser Source URL for OBS will be displayed on the screen.

By default, the url should be http://localhost:3002

### Running the Dungeon

As you or anyone else runs the dungeon, and you are nearby, the cards will automatically 
show up in the OBS Browser Source.

You can open up the OBS Browser Source URL in a normal browser too and use it as a dungeon companion.

## Commands

### Set Dungeon Key Barrel Position

```plaintext
/deckedout dungeon set <key barrel position>
```

- `<key barrel position>`: The x, y, z coordinates of the key barrel.

### Change Port Number

```plaintext
/deckedout port set <port>
```

- `<port>`: The port number you want to set for the server.

## Usage with OBS

1. **Add Browser Source**: In OBS, add a new Browser Source.
2. **Enter URL**: Use the URL displayed on your screen after starting the server.
3. **Adjust Settings**: Set the width and height as needed.

For the best experience with a 1080p stream, try setting the width to 720 and the height to 1000.
Once that's set, you can adjust the position and scale of the Browser Source to fit your stream layout.

![obs setup](/docs/obs.png)

## Troubleshooting

- **Cards Not Showing**: Ensure you are near the dungeon and that the mod is correctly calibrated.
- **OBS URL Not Displayed**: Make sure the server is running and check for any error messages in the game.

## Contributing

If you encounter any issues or have suggestions for improvements, feel free to open an issue or pull request on the GitHub repository.

## License

This project is licensed under the GPL-3 License.

## Disclaimer

The card artwork has been created for TangoTek by [CJ Moseley](https://cjmoseley.co.uk/) and has been 
extracted from the world download of the Hermitcraft Season 9 server for this project.

---

**Enjoy running the Decked Out dungeon with enhanced visuals for your audience!**

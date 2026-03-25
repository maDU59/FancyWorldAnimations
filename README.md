# Fancy World Animations
FWA in short, is a mod that adds animations to user-interactable blocks like doors, fence-gates, cauldrons, lecterns, chests and much more!

It's the mod you never knew you needed until now, once you try it it's hard to go back!

**Compatible with resource packs, Sodium and Iris shaders**

<table>
<tr>
<td width="50%">
<img src="https://cdn.modrinth.com/data/IAzUFvS6/images/97dde016ea4f84158a8ea47802dc6e0ba67b8978.gif">
</td>
<td width="50%">
<img src="https://cdn.modrinth.com/data/IAzUFvS6/images/e33e6b3d4dc16ba72369d90ede8543f012342cc6.gif">
</td>
</tr>
  </table>
  <table>
<tr>
<td width="50%">
<img src="https://cdn.modrinth.com/data/IAzUFvS6/images/bc41e76047e79be41fe6b2290dab29df58a9e380.gif">
</td>
<td width="50%">
<img src="https://cdn.modrinth.com/data/IAzUFvS6/images/e219109d4371a777697672db06267573db49f0f7.gif">
</td>
</tr>
</table>

<details>
<summary>All the animations</summary>

<ul>
<li>Smooth doors</li>
<li>Smooth trapdoors</li>
<li>Smooth fence-gates</li>
<li>Smooth levers</li>
<li>Smooth buttons</li>
<li>Smooth repeaters</li>
<li>Smooth cauldrons</li>
<li>Smooth composters</li>
<li>Smooth lecterns</li>
<li>Smooth campfires</li>
<li>Smooth chiseled bookshelves</li>
<li>Disc animation on Jukeboxes</li>
<li>Idling lecterns</li>
<li>Idling bells</li>
<li>Eye-preview on end portal frames</li>
<li>Opening animation on vaults</li>
<li>Customisable chest animation</li>
<li>Swinging lanterns</li>
<li>Swinging chains</li>
</ul>

</details>

## ⚙️ Configuration

To edit the settings of the mod:
- Use Mod Menu
- Use the command `/fwaConfig` to open the config screen
- Edit the config file in `.minecraft/config/fwa.json`

## 🔧 Compatibility

**This mod tries to be as compatible as possible with resource packs and shaderpacks.**
<details>
<summary>Non-exhaustive list of supported mods:</summary>

<ul>
<li>Dramatic Doors</li>
<li>Create</li>
<li>More Composter Variants</li>
</ul>

</details>


## 📦 Modpack

Feel free to add this mod in your modpacks. If you do so, telling me would also be nice.</br>
In case of conflict with another mod, you can disable animations on specific blocks/mods using a config file in the config folder `fwa-blacklist.json`.</br>
Example:
```json
{
    "mods":[
        "create"
    ],
    "blocks":[
        "minecraft:spruce_door"
    ]
}
```
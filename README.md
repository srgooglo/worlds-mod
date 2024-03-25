# Worlds
Multiverse-core plugin but actually is a fabric mod.

# Compatibility
*just for Minecraft 1.20.1 at the moment*

Use Fabric API version +0.92.0

# Dependencies
May need import the lib
[https://github.com/NucleoidMC/fantasy/releases/tag/v0.4.11%2B1.20-rc1](https://github.com/NucleoidMC/fantasy/releases/tag/v0.4.11%2B1.20-rc1)


# Commands usage
## OP Commands
### Create a new dimension
``
/aworlds create <id> <type [NORMAL|NETHER|END]> <seed>
``

### Set current world spawn
``
/aworlds setWorldSpawn
``

### Delete a dimension [BROKEN]
> For the moment you can delete the dimensions by navigating to the mod configuration directory `config/worlds/dimensions/worlds_dimensions/<id>` deleting the .json file of the dimension you want to delete.

> You can also destroy the chunks from your `world/dimensions/worlds_dimensions/<id>` directory.

``
/aworlds delete <id>
``

## User Commands
### List all dimensions
``
/worlds list
``

### Teleport to a dimension
``
/worlds tp <id>
``

### Teleport to current dimension spawn
``
/worlds spawn
``

### Get current dimension info
``
/worlds current
``

# Other info
Using the lib implementation [xyz.nucleoid:fantasy](https://github.com/NucleoidMC/fantasy)
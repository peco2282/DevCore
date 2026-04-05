package com.peco2282.devcore.packet

import com.peco2282.devcore.packet.entity.EntityManagerHub
import com.peco2282.devcore.packet.environment.EnvironmentHub
import com.peco2282.devcore.packet.interact.InteractHub
import com.peco2282.devcore.packet.inventory.InventoryHub
import com.peco2282.devcore.packet.vfx.VfxHub
import com.peco2282.devcore.packet.view.ViewHub

/**
 * Central interface for all packet-level operations.
 *
 * Combines connection, entity, environment, interaction, inventory, visual effects, 
 * view, and miscellaneous sub-hubs into a single API entry point.
 */
interface PacketHub : 
  ConnectionHub, 
  EntityManagerHub, 
  EnvironmentHub, 
  InteractHub, 
  InventoryHub, 
  VfxHub, 
  ViewHub, 
  MiscHub

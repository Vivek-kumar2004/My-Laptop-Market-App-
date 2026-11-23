import hp from "./assets/product-hp.jpg?inline";
import dell from "./assets/product-dell.jpg?inline";
import thinkpad from "./assets/product-thinkpad.jpg?inline";
import macbook from "./assets/product-macbook.jpg?inline";
import server from "./assets/product-server.jpg?inline";
import monitor from "./assets/product-monitor.jpg?inline";
import network from "./assets/product-network.jpg?inline";
import lifestyle from "./assets/lifestyle-refurb.jpg?inline";

export const images = {
  hp,
  dell,
  thinkpad,
  macbook,
  server,
  monitor,
  network,
  lifestyle,
};

export type AssetKey = keyof typeof images;

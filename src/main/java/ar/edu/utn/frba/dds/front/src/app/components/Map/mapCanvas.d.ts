import type React from 'react';
import type { HechoFeature } from './dataMocks';

export type MapCanvasHandle = {
  flyTo: (coords: { lat: number; lng: number }) => void;
  flyToById: (id: string) => void;
};

export type MapCanvasProps = {
  features: HechoFeature[];
  onFeatureClick?: (f: HechoFeature) => void;
  search: string;
  onSearchChange: React.Dispatch<React.SetStateAction<string>>;
  onToggleFilters: () => void;
};

declare const MapCanvas: React.ForwardRefExoticComponent<
  React.PropsWithoutRef<MapCanvasProps> & React.RefAttributes<MapCanvasHandle>
>;

export default MapCanvas;

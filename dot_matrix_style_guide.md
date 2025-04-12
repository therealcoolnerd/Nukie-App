# Nukie Dot Matrix Style Guide

## Overview
The Nukie social media aggregator features a distinctive dot matrix visual style inspired by retro displays while maintaining a modern, clean aesthetic. This style guide defines how to implement this visual language consistently across the application.

## Core Principles
1. **Dot-Based Rendering**: All visual elements should be composed of or appear to be composed of individual dots
2. **Legibility**: Despite the dot matrix style, all text and UI elements must remain highly legible
3. **Utilitarian**: Focus on function while maintaining visual appeal
4. **Modern Interpretation**: A contemporary take on dot matrix displays, not purely retro

## Color Palette

### Primary Colors
- **Nukie Orange**: #FF5733 (Used in logo and primary actions)
- **Background Cream**: #FFF8E7 (Base background color)
- **Dark Gray**: #333333 (Text and UI elements)

### Secondary Colors
- **Light Orange**: #FFAA80 (Highlights and secondary elements)
- **Soft Black**: #1A1A1A (For contrast and depth)
- **Accent Blue**: #4A90E2 (For notifications and special elements)

## Typography
- **Primary Font**: "Dot Matrix" (custom font for headings and emphasis)
- **Secondary Font**: "Roboto Mono" (for body text and UI elements)
- **Fallback**: Monospace system fonts

### Text Rendering
- Headlines: Larger dot matrix with visible dot structure
- Body text: Smaller dot matrix with higher density for better readability
- UI elements: Medium density dot matrix

## UI Elements

### Buttons
- Rectangular with slightly rounded corners
- Dot matrix border with solid or semi-transparent fill
- Clear hover and active states with dot density changes

### Cards and Containers
- Light backgrounds with dot matrix borders
- Subtle dot pattern in backgrounds
- Clear visual hierarchy through dot density variations

### Icons
- Custom dot matrix icons for primary functions
- Simplified designs optimized for dot matrix rendering
- Consistent sizing and padding

### Loading Indicators and Animations
- Dot-based animations that build or flow
- Avoid smooth transitions in favor of dot-by-dot reveals
- Maintain the "digital" feel in all motion

## Implementation Techniques

### CSS Approach
- Use of CSS grid for precise dot positioning
- Custom dot matrix backgrounds using repeating patterns
- SVG filters for dot matrix effect on dynamic content

### Image Assets
- SVG format for UI elements to maintain crispness at all sizes
- PNG with transparency for complex elements
- Consistent dot size and spacing across all assets

### Responsive Considerations
- Adjust dot density based on screen size and resolution
- Maintain legibility on smaller screens by increasing relative dot size
- Ensure touch targets remain accessible despite dot matrix styling

## Examples

### Text Rendering
```
H E A D L I N E
• • • • • • • • •
• • • • • • • • •
```

### Button States
```
[ N O R M A L ]  [ H O V E R ]  [ A C T I V E ]
• • • • • • • •  • • • • • • •  • • • • • • •
•           •  •           •  • • • • • • •
•           •  •           •  • • • • • • •
• • • • • • • •  • • • • • • •  • • • • • • •
```

### Loading Animation
```
Frame 1    Frame 2    Frame 3
• • • •    • • • •    • • • •
•     •    • •   •    • • • •
•     •    •     •    • •   •
• • • •    • • • •    • • • •
```

## Application in UI Components

### Navigation
- Bottom navigation bar with dot matrix icons
- Active state indicated by increased dot density
- Simple animations for transitions

### Feed Display
- Content cards with dot matrix borders
- Media thumbnails with dot overlay filter
- Text rendered in appropriate dot density based on hierarchy

### User Profile
- Avatar with dot matrix frame
- Stats and metrics displayed in dot-based counters
- Settings toggles with dot matrix styling

This style guide serves as the foundation for creating a consistent, unique visual identity for the Nukie social media aggregator while ensuring usability and modern appeal.

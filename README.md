# 🌍 Pepse Game

Pepse (Procedural Endless Procedurally Side-scrolling Environment) is a Java-based **2D side-scrolling game**.  
It features an endless world with terrain, trees, clouds, day-night cycles, and an avatar that the player controls.

---

## 📌 Features
- 🕹️ **Playable Avatar**  
  - Movement and jumping animations.  
  - Energy system displayed on screen.  

- 🌞 **Dynamic World**  
  - Procedural terrain generation.  
  - Clouds, trees, and fruits appear naturally.  
  - Day and night cycle with sun and moon effects.  

- 🌱 **Interactive Environment**  
  - Trees with leaves and fruits.  
  - Fruits can be collected/eaten for energy.  

- 🎨 **Custom Assets**  
  - Smooth animations for idle, running, and jumping.  
  - Natural color variations (using noise generation).  

---

## 📂 Project Structure
```
pepse/
 ├── PepseGameManager.java      # Main game manager (entry point)
 ├── util/                      # Utility classes (colors, noise, energy display)
 ├── world/                     # World generation
 │   ├── Avatar.java            # Player character
 │   ├── Block.java             # Terrain blocks
 │   ├── Cloud.java             # Cloud objects
 │   ├── Sky.java               # Background sky
 │   ├── Terrain.java           # Procedural terrain
 │   ├── daynight/              # Day-night cycle (Sun, Halo, Night)
 │   └── trees/                 # Trees, leaves, fruits
assets/                         # Sprite animations (idle, run, jump)
```

---

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/Abdshalbe/pepseGame.git
cd pepseGame
```

### 2. Compile the project
```bash
javac -d bin $(find pepse -name "*.java")
```

### 3. Run the game
```bash
java -cp bin pepse.PepseGameManager
```

---

## 🖥️ Gameplay
- Use **arrow keys** or **WASD** to move the avatar.  
- Collect fruits 🍎 to restore energy.  
- Watch the environment change as **day turns into night**.  
- Explore an **infinite terrain** with clouds, trees, and natural scenery.  

---

## 🛠️ Technologies
- **Java** (OOP, modular design).  
- **Procedural generation** for terrain and colors.  
- **Sprite-based animation** for smooth character movement.  
- **Noise functions** for natural randomness.  

---

## 📖 Future Enhancements
- Add enemies or hazards.  
- Add scoring and health system.  
- Expand fruit mechanics (different effects).  
- Sound effects and background music.  
- Save/load game state.  

---

## 👤 Author
Developed by **[Abd Shalbe,Taqi obidat](https://github.com/Abdshalbe,https://github.com/Taqi2002)**  

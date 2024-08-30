




import kotlin.math.*

// Constants
const val GRAVITY = 9.81 // m/s^2
const val GAS_CONSTANT = 287.05 // J/(kg*K)
const val SEA_LEVEL_PRESSURE = 101325.0 // Pa
const val SEA_LEVEL_TEMPERATURE = 288.15 // K

// Rocket Engine Parameters
const val THROAT_DIAMETER = 0.05 // m
const val BURN_RATE = 0.01 // kg/s
const val CHAMBER_PRESSURE = 1000000.0 // Pa
const val BURN_TIME = 120.0 // s
const val MOLAR_MASS_AIR = 0.0289644 // kg/mol
const val GAMMA = 1.4
const val COMBUSTION_EFFICIENCY = 0.95

// Advanced Parameters
const val EXIT_DIAMETER_MIN = 0.1 // m
const val EXIT_DIAMETER_MAX = 0.2 // m

// Function to calculate specific impulse
fun specificImpulse(exitVelocity: Double): Double = exitVelocity / GRAVITY

// Function to calculate exit velocity
fun calculateExitVelocity(chamberPressure: Double, exitDiameter: Double): Double {
    val exitPressure = chamberPressure / (1 + (GAMMA - 1) / 2)
    val exitTemperature = SEA_LEVEL_TEMPERATURE * (exitPressure / SEA_LEVEL_PRESSURE).pow((GAMMA - 1) / GAMMA)
    return sqrt(2 * GAMMA * GAS_CONSTANT * exitTemperature * (1 - (exitPressure / chamberPressure).pow((GAMMA - 1) / GAMMA)))
}

// Function to calculate thrust with variable geometry nozzle
fun calculateThrust(chamberPressure: Double, exitDiameter: Double): Double {
    val exitVelocity = calculateExitVelocity(chamberPressure, exitDiameter)
    val throatArea = PI * (THROAT_DIAMETER / 2).pow(2)
    val exitArea = PI * (exitDiameter / 2).pow(2)
    return throatArea * (chamberPressure * exitArea - chamberPressure * throatArea) + exitArea * chamberPressure * (exitDiameter / 2)
}

// Function to simulate propellant consumption and chamber pressure dynamics with variable geometry nozzle
fun simulateEngineOperation(initialPressure: Double, burnTime: Double): Pair<List<Double>, List<Double>> {
    var chamberPressure = initialPressure
    var exitDiameter = EXIT_DIAMETER_MIN // Start with minimum exit diameter
    val timeSteps = mutableListOf<Double>()
    val pressures = mutableListOf<Double>()

    var time = 0.0
    while (time <= burnTime) {
        val thrust = calculateThrust(chamberPressure, exitDiameter)
        val massFlowRate = BURN_RATE
        val exitPressure = chamberPressure / (1 + (GAMMA - 1) / 2)
        val throatArea = PI * (THROAT_DIAMETER / 2).pow(2)
        val exitArea = PI * (exitDiameter / 2).pow(2)
        val deltaPressure = massFlowRate * calculateExitVelocity(chamberPressure, exitDiameter) + (exitPressure - chamberPressure) * (throatArea / exitArea)
        chamberPressure += deltaPressure

        // Adjust exit diameter based on chamber pressure
        exitDiameter = mapExitDiameter(chamberPressure)

        timeSteps.add(time)
        pressures.add(chamberPressure)

        time += 1.0
    }

    return Pair(timeSteps, pressures)
}

// Map exit diameter based on chamber pressure
fun mapExitDiameter(chamberPressure: Double): Double {
    val pressureRatio = (chamberPressure - CHAMBER_PRESSURE) / CHAMBER_PRESSURE
    return EXIT_DIAMETER_MIN + pressureRatio * (EXIT_DIAMETER_MAX - EXIT_DIAMETER_MIN)
}

fun main() {
    val (timeSteps, pressures) = simulateEngineOperation(CHAMBER_PRESSURE, BURN_TIME)

    val totalImpulse = timeSteps.zip(pressures).map { (time, pressure) -> calculateThrust(pressure, mapExitDiameter(pressure)) * time }.sum()

    val exitVelocity = calculateExitVelocity(pressures.last(), mapExitDiameter(pressures.last()))
    val specificImpulse = specificImpulse(exitVelocity)

    println("Simulation Results:")
    println("===================")
    println("Total Impulse: $totalImpulse Ns")
    println("Specific Impulse: $specificImpulse s")
    println("\nSimulation Details:")
    println("===================")
    println("Chamber Pressure: ${pressures.last()} Pa")
    println("Exit Diameter: ${mapExitDiameter(pressures.last())} m (at end of burn)")
    println("Exit Velocity: $exitVelocity m/s")
    println("Burn Time: $BURN_TIME s")
    println("Throat Diameter: $THROAT_DIAMETER m")
    println("Exit Diameter Range: $EXIT_DIAMETER_MIN m to $EXIT_DIAMETER_MAX m")
    println("Burn Rate: $BURN_RATE kg/s")
    println("\nRocket Engine Simulation Completed.")
}

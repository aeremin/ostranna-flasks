package `in`.aerem.ostranna_flasks

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import io.ktor.util.logging.*
import java.io.BufferedReader
import java.io.InputStreamReader

interface ComPort {
  abstract fun write(s: String)
  abstract fun readLine(): String
}

class StubComPort: ComPort {
  override fun write(s: String) {
      // Do nothing
  }

  override fun readLine(): String {
      return "Ack 0"
  }
}

class SerialComPort: ComPort {
  private val serialPort: SerialPort
  private val reader: BufferedReader

  constructor(serialPort: SerialPort) {
      this.serialPort = serialPort
      this.reader = BufferedReader(InputStreamReader(serialPort.inputStream))
  }

  override fun write(s: String) {
      if (s.length != serialPort.writeBytes(s.toByteArray(), s.length.toLong())) {
      throw Exception("Failure when sending '$s' to serial port, error code: ${serialPort.lastErrorCode}")
      }
  }

  override fun readLine(): String {
      return reader.readLine()
  }
}

fun openComport(log: Logger, comPortName: String): ComPort {
  if (comPortName == "stub") return StubComPort()

  var comPortNameChosen = comPortName
  if (comPortName == "auto") {
      val allPorts = SerialPort.getCommPorts()
      if (allPorts.size == 0) {
          throw Exception("'auto' setting for com port is used, but no com ports are detected")
      }
      if (allPorts.size > 1) {
          throw Exception("'auto' setting for com port is used, but multiple com ports detected: ${allPorts.joinToString { it.systemPortName }}")
      }
      comPortNameChosen = allPorts[0].systemPortName
  }

  val comport = SerialPort.getCommPort(comPortNameChosen)
  comport.baudRate = 115200
  comport.numStopBits = SerialPort.ONE_STOP_BIT
  if (!comport.openPort()) {
      throw Exception("Can't connect to serial device at $comPortNameChosen, devices available: ${SerialPort.getCommPorts().joinToString { it.systemPortName }}")
  }
  if (!comport.addDataListener(object: SerialPortDataListener {
      override fun getListeningEvents(): Int {
          return SerialPort.LISTENING_EVENT_DATA_RECEIVED
      }

      override fun serialEvent(event: SerialPortEvent?) {
          if (event == null) return
          log.info("Received from COM port: ${String(event.receivedData).trim()}")
      }
  })) {
      throw Exception("Failed to start listening at $comPortNameChosen")
  }
  log.info("Successfully connected to $comPortNameChosen, listening to data")
  return SerialComPort(comport)
}

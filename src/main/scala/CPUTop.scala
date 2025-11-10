import chisel3._
import chisel3.util._

class CPUTop extends Module {
  val io = IO(new Bundle {
    val done = Output(Bool ())
    val run = Input(Bool ())
    //This signals are used by the tester for loading and dumping the memory content, do not touch
    val testerDataMemEnable = Input(Bool ())
    val testerDataMemAddress = Input(UInt (16.W))
    val testerDataMemDataRead = Output(UInt (32.W))
    val testerDataMemWriteEnable = Input(Bool ())
    val testerDataMemDataWrite = Input(UInt (32.W))
    //This signals are used by the tester for loading and dumping the memory content, do not touch
    val testerProgMemEnable = Input(Bool ())
    val testerProgMemAddress = Input(UInt (16.W))
    val testerProgMemDataRead = Output(UInt (32.W))
    val testerProgMemWriteEnable = Input(Bool ())
    val testerProgMemDataWrite = Input(UInt (32.W))
  })

  //Creating components
  val programCounter = Module(new ProgramCounter())
  val dataMemory = Module(new DataMemory())
  val programMemory = Module(new ProgramMemory())
  val registerFile = Module(new RegisterFile())
  val controlUnit = Module(new ControlUnit())
  val alu = Module(new ALU())

  //Connecting the modules
  programCounter.io.run := io.run
  programMemory.io.address := programCounter.io.programCounter

  ////////////////////////////////////////////
  //Continue here with your connections
  ////////////////////////////////////////////

  //fetch
  // meaning program counter provides address to program memory
  // sends fetched instruction from program memory to control unit
  controlUnit.io.instr := programMemory.io.instructionRead(15, 0)

  //decode and execution
  // register file selector connections from control unit
  registerFile.io.aSel := controlUnit.io.rsSel
  registerFile.io.bSel := controlUnit.io.rtSel

  //ALU inputs from register file outputs
  alu.io.a := registerFile.io.a
  alu.io.b := registerFile.io.b

  //ALU operation selector
  alu.io.sel := controlUnit.io.aluSel

  //memory
  //ALU results  used as  address for DataMemory
  // load and store instructions
  dataMemory.io.address := alu.io.out
  dataMemory.io.dataWrite := registerFile.io.b
  dataMemory.io.writeEnable := controlUnit.io.memWrite

  //write back
  // chooses whether to write ALU result or memory output back to registers

  // for LI
  val immediate = controlUnit.io.instr(7,0)

  // zero-extend the 8-bit immediate to 16 bits by adding 8 zeros in front.
  // ensures it fits in 16bit
  val extendedImm = Cat(0.U(8.W), immediate)

  // temp 16-bit wire to hold whichever value we will write back to the register file.
  val writeBackData = Wire(UInt(16.W))

  // MUX
  // default -> ALU result
  // if useMemData = true  -> take data from memory (LD)
  // if useImmediate = true -> take the immediate constant (LI)
  writeBackData := MuxCase(alu.io.out, Seq(
    controlUnit.io.useMemData -> dataMemory.io.dataRead(15, 0),
    controlUnit.io.useImmediate -> extendedImm
  ))

  registerFile.io.writeEnable := controlUnit.io.regWrite
  registerFile.io.writeSel := controlUnit.io.rdSel
  registerFile.io.writeData := writeBackData

  // program counter
  programCounter.io.stop := controlUnit.io.pcStop
  programCounter.io.jump := controlUnit.io.pcJump
  programCounter.io.programCounterJump := registerFile.io.a

  // done signal
  io.done := controlUnit.io.pcStop

  //This signals are used by the tester for loading the program to the program memory, do not touch
  programMemory.io.testerAddress := io.testerProgMemAddress
  io.testerProgMemDataRead := programMemory.io.testerDataRead
  programMemory.io.testerDataWrite := io.testerProgMemDataWrite
  programMemory.io.testerEnable := io.testerProgMemEnable
  programMemory.io.testerWriteEnable := io.testerProgMemWriteEnable
  //This signals are used by the tester for loading and dumping the data memory content, do not touch
  dataMemory.io.testerAddress := io.testerDataMemAddress
  io.testerDataMemDataRead := dataMemory.io.testerDataRead
  dataMemory.io.testerDataWrite := io.testerDataMemDataWrite
  dataMemory.io.testerEnable := io.testerDataMemEnable
  dataMemory.io.testerWriteEnable := io.testerDataMemWriteEnable
}
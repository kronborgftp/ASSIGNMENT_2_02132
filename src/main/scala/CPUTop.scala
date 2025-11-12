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
  // MEMORY STAGE
  // For LD and SD, ALU result provides the effective address.
  // For SD: mem[rs] = rd
  // For LD: rd = mem[rs]

  when (controlUnit.io.memWrite) {
    // SD (store): address = value in rd, data = value in rs
    dataMemory.io.address := registerFile.io.a   // aSel = rdSel
    dataMemory.io.dataWrite := registerFile.io.b // bSel = rsSel
  } .elsewhen (controlUnit.io.useMemData) {
    // LD (load): address = value in rs
    dataMemory.io.address := registerFile.io.a   // aSel = rsSel
    dataMemory.io.dataWrite := 0.U               // not used for load
  } .otherwise {
    // All other instructions: use ALU output
    dataMemory.io.address := alu.io.out
    dataMemory.io.dataWrite := registerFile.io.b
  }


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
  // Conditional jump: only jump if rs (register b) != 0
  programCounter.io.jump := controlUnit.io.pcJump && (registerFile.io.b =/= 0.U)
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

  ////////////////////////////////////////////
  // Debug instrumentation (fixed)
  ////////////////////////////////////////////

  // simple cycle counter
  val cycle = RegInit(0.U(32.W))
  cycle := cycle + 1.U

  // prepare values to show
  val instr16 = controlUnit.io.instr               // 16-bit view
  val pc = programCounter.io.programCounter
  val aluOut = alu.io.out
  val wsel = controlUnit.io.rdSel
  val regWr = controlUnit.io.regWrite
  val regWriteData = registerFile.io.writeData
  val memData = dataMemory.io.dataRead(15,0)
  val imm8 = controlUnit.io.instr(7,0)
  val immExt = Cat(0.U(8.W), imm8)                  // same as extendedImm

  when (io.run && !io.done) {
    // default: ALU (no immediate, no mem)
    when (controlUnit.io.useMemData) {
      // memory was used for writeback
      printf(p"[Cycle $cycle] PC=${pc} Instr=${Binary(instr16)} ALUout=${aluOut} RegWrite=${regWr} WriteSel=${wsel} WriteData=${regWriteData} Src=MEM MemData=${memData}\n")
    } .elsewhen (controlUnit.io.useImmediate) {
      // immediate was used for writeback
      printf(p"[Cycle $cycle] PC=${pc} Instr=${Binary(instr16)} ALUout=${aluOut} RegWrite=${regWr} WriteSel=${wsel} WriteData=${regWriteData} Src=IMM Imm=${immExt}\n")
    } .otherwise {
      // ALU result was used
      printf(p"[Cycle $cycle] PC=${pc} Instr=${Binary(instr16)} ALUout=${aluOut} RegWrite=${regWr} WriteSel=${wsel} WriteData=${regWriteData} Src=ALU\n")
    }
    when (controlUnit.io.memWrite) {
      printf(p"[SD] A=${alu.io.a} B=${alu.io.b} Addr=${alu.io.out} Data=${registerFile.io.b}\n")
    }
    when (registerFile.io.writeEnable) {
      printf(p"[WRITEBACK] cycle=$cycle PC=${programCounter.io.programCounter} writeSel=${registerFile.io.writeSel} writeData=${registerFile.io.writeData}\n")
    }
    when (controlUnit.io.memWrite) {
      printf(p"[STORE] cycle=$cycle A=${registerFile.io.a} B=${registerFile.io.b} Addr=${dataMemory.io.address} Data=${dataMemory.io.dataWrite}\n")
  }}


}
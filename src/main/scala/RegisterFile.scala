import chisel3._
import chisel3.util._

class RegisterFile extends Module {
  val io = IO(new Bundle {
    val aSel = Input(UInt(4.W))        // selects source register 1 (rs)
    val bSel = Input(UInt(4.W))        // selects source register 2 (rt)
    val writeSel = Input(UInt(4.W))    // selects destination register (rd)
    val writeEnable = Input(Bool())    // enables writing on clock edge
    val writeData = Input(UInt(16.W))  // 16-bit data to write
    val a = Output(UInt(16.W))         // output of rs
    val b = Output(UInt(16.W))         // output of rt
  })

  // Create 16 registers, each 16 bits wide
  val regs = RegInit(VecInit(Seq.fill(16)(0.U(16.W))))

  // Combinational reads (asynchronous)
  io.a := regs(io.aSel)
  io.b := regs(io.bSel)

  // Synchronous write (on rising clock edge)
  when(io.writeEnable) {
    regs(io.writeSel) := io.writeData
  }
}

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.util

object Programs{
  val program1 = Array(
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W)
  )

  val program2 = Array(
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W)
  )

  //TEST PROGRAM!!!!
  val testProgram = Array(
    "h8310".U(32.W), // LD R3, R1       (load from address in R1)
    "h0534".U(32.W), // ADD R5, R3, R4  (R5 = R3 + R4)
    "h9250".U(32.W), // SD R2, R5       (store R5 to address in R2)
    "hF000".U(32.W)  // STOP            (halt execution)
  )
}
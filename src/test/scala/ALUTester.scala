import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/*
 * to run type this in the IntelliJ terminal :
 *   sbt "testOnly ALUTester"
 */

//USES THE SAME PATTERN AND LOGIC FROM PromCounterTester. THANK YOU LUCA, MY METHOD WASN'T WORKING

class ALUTester extends AnyFlatSpec with ChiselScalatestTester {

  "ALU" should "perform 16-bit arithmetic and logic operations correctly" in {

    // Instantiate the ALU and run the test
    test(new ALU()) { dut =>

      // ADD TEST
      // Expected: 10 + 3 = 13
      dut.io.a.poke(10.U)
      dut.io.b.poke(3.U)
      dut.io.sel.poke("b0001".U)  // ADD opcode
      dut.clock.step(1)
      dut.io.out.expect(13.U)
      println("ADD test passed (10 + 3 = 13)")

      // SUB TEST
      // Expected: 10 - 3 = 7
      dut.io.a.poke(10.U)
      dut.io.b.poke(3.U)
      dut.io.sel.poke("b0010".U)  // SUB opcode
      dut.clock.step(1)
      dut.io.out.expect(7.U)
      println("SUB test passed (10 - 3 = 7)")

      // OR TEST
      // Expected: 10 (1010) | 3 (0011) = 11 (1011)
      dut.io.a.poke(10.U)
      dut.io.b.poke(3.U)
      dut.io.sel.poke("b0011".U)  // OR opcode
      dut.clock.step(1)
      dut.io.out.expect((10 | 3).U)
      println("OR test passed (10 | 3 = 11)")

      // AND TEST
      // Expected: 10 (1010) & 3 (0011) = 2 (0010)
      dut.io.a.poke(10.U)
      dut.io.b.poke(3.U)
      dut.io.sel.poke("b0100".U)  // AND opcode
      dut.clock.step(1)
      dut.io.out.expect((10 & 3).U)
      println("AND test passed (10 & 3 = 2)")

      // NOT TEST
      // Input: a = 10 (0000 0000 0000 1010)
      // Expected: 65525 (1111 1111 1111 0101)
      dut.io.a.poke(10.U)
      dut.io.b.poke(0.U)             // b unused in NOT
      dut.io.sel.poke("b0101".U)     // NOT opcode
      dut.clock.step(1)
      dut.io.out.expect(65525.U)     // expected 16-bit inverted value
      println("NOT test passed (~10 = 65525)")

      // GREATER THAN TEST
      // Expected: (10 > 3) ? 1 : 0  -> should return 1
      dut.io.a.poke(10.U)
      dut.io.b.poke(3.U)
      dut.io.sel.poke("b0110".U)  // GT opcode
      dut.clock.step(1)
      dut.io.out.expect(1.U)
      println("GT test passed (10 > 3 -> 1)")

      // MODULO TEST
      // Expected: 10 % 3 = 1
      dut.io.a.poke(10.U)
      dut.io.b.poke(3.U)
      dut.io.sel.poke("b0111".U)  // MOD opcode
      dut.clock.step(1)
      dut.io.out.expect((10 % 3).U)
      println("MOD test passed (10 % 3 = 1)")

      // ZERO FLAG TEST
      // Test the zero flag output by setting a = b (so result = 0)
      dut.io.a.poke(5.U)
      dut.io.b.poke(5.U)
      dut.io.sel.poke("b1111".U)  // SUB (5 - 5 = 0)
      dut.clock.step(1)
      dut.io.out.expect(0.U)
      dut.io.zero.expect(true.B)
      println("ZERO flag test passed (5 - 5 = 0 -> zero = true)")

      // END OF TEST
      println("all ALU tests passed")
    }
  }
}


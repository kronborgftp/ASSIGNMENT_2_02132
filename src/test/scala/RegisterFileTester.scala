import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/*
 * To run:
 *   sbt "testOnly RegisterFileTester"
 * output:
 *  /test_run_dir/RegisterFileTester/RegisterFile.vcd
 */

class RegisterFileTester extends AnyFlatSpec with ChiselScalatestTester {

  "RegisterFile" should "perform read and write correctly" in {
    test(new RegisterFile())
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

        dut.clock.setTimeout(0)

        // STEP 1: Write 10 into R3
        dut.io.writeEnable.poke(true.B)
        dut.io.writeSel.poke(3.U)
        dut.io.writeData.poke(10.U)
        dut.clock.step(1)

        // STEP 2: Write 255 into R5
        dut.io.writeSel.poke(5.U)
        dut.io.writeData.poke(255.U)
        dut.clock.step(1)

        // STEP 3: Disable write and read both
        dut.io.writeEnable.poke(false.B)
        dut.io.aSel.poke(3.U)
        dut.io.bSel.poke(5.U)
        dut.clock.step(1)

        // Expect read values
        dut.io.a.expect(10.U)
        dut.io.b.expect(255.U)
        println("RegisterFile basic read/write passed")

        // STEP 4: Overwrite R3 with 42
        dut.io.writeEnable.poke(true.B)
        dut.io.writeSel.poke(3.U)
        dut.io.writeData.poke(42.U)
        dut.clock.step(1)

        // STEP 5: Verify new value
        dut.io.writeEnable.poke(false.B)
        dut.io.aSel.poke(3.U)
        dut.clock.step(1)
        dut.io.a.expect(42.U)
        println("Overwrite test passed")

        println("All RegisterFile tests passed")
      }
  }
}


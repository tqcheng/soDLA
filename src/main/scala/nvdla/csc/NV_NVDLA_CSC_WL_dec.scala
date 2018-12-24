package nvdla

import chisel3._
import chisel3.experimental._
import chisel3.util._

class NV_NVDLA_CSC_single_reg(implicit val conf: cscConfiguration) extends RawModule {
    val io = IO(new Bundle {
        //general clock
        val nvdla_core_clk = Input(Clock())      
        val nvdla_core_rstn = Input(Bool())

        // Register control interface
        val input_data = Input(UInt((conf.CSC_ATOMC * conf.CSC_BPE).W))
        val input_mask = Input(UInt(conf.CSC_ATOMC.W))
        val input_mask_en = Input(UInt(10.W))
        val input_pipe_valid = Input(Bool())

        val reg_wr_en = Input(Bool())

        // Writable register flop/trigger outputs

        val producer = Output(Bool())

        // Read-only register inputs

        val consumer = Input(Bool())
        val status_0 = Input(UInt(2.W))
        val status_1 = Input(UInt(2.W))       
    })
//     
//          ┌─┐       ┌─┐
//       ┌──┘ ┴───────┘ ┴──┐
//       │                 │
//       │       ───       │          
//       │  ─┬┘       └┬─  │
//       │                 │
//       │       ─┴─       │
//       │                 │
//       └───┐         ┌───┘
//           │         │
//           │         │
//           │         │
//           │         └──────────────┐
//           │                        │
//           │                        ├─┐
//           │                        ┌─┘    
//           │                        │
//           └─┐  ┐  ┌───────┬──┐  ┌──┘         
//             │ ─┤ ─┤       │ ─┤ ─┤         
//             └──┴──┘       └──┴──┘ 
withClockAndReset(io.nvdla_core_clk, !io.nvdla_core_rstn){

    // Address decode

    val nvdla_csc_s_pointer_0_wren = (io.reg_offset === "h4".asUInt(32.W))&io.reg_wr_en
    val nvdla_csc_s_status_0_wren = (io.reg_offset === "h0".asUInt(32.W))&io.reg_wr_en
    val nvdla_csc_s_pointer_0_out = Cat("b0".asUInt(15.W), io.consumer, "b0".asUInt(15.W), io.producer)
    val nvdla_csc_s_status_0_out = Cat("b0".asUInt(14.W), io.status_1, "b0".asUInt(14.W), io.status_0)

    // Output mux
   
    io.reg_rd_data := MuxLookup(io.reg_offset, "b0".asUInt(32.W), 
    Seq(      
    "h4".asUInt(32.W)  -> nvdla_csc_s_pointer_0_out,
    "h0".asUInt(32.W)  -> nvdla_csc_s_status_0_out
    ))

    // Register flop declarations

    val producer_out = RegInit(false.B)

    when(nvdla_csc_s_pointer_0_wren){
        producer_out:= io.reg_wr_data(0)
    }
        
    io.producer := producer_out
    
}}


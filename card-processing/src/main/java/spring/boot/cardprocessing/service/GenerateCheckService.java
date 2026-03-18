package spring.boot.cardprocessing.service;

import spring.boot.cardprocessing.dto.CheckDTO;



public interface GenerateCheckService {

  byte[] generateCheck(CheckDTO transactionDTO);

}

package com.ewallet.demo.Controller;

import com.ewallet.demo.Model.Transaction;
import com.ewallet.demo.Model.User;
import com.ewallet.demo.Model.Wallet;
import com.ewallet.demo.Repository.TransactionRepository;
import com.ewallet.demo.Repository.WalletRepository;
import com.ewallet.demo.ThirdPartyServices.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    //sendMoney
    @PostMapping("/sendMoney")
    public Transaction sendMoney(@RequestBody Transaction transaction) throws Exception{
        //from the request transaction body, we will get sender id
        // and receiver id and corresponding users by using our another user service running
        //on diff spring boot application.
        //and update their wallets.
        User sender = userService.findUserById(transaction.getSender_id());//1
        User receiver = userService.findUserById(transaction.getReceiver_id());//2
        //rollback logic
        if(sender ==null || receiver ==null){
            LOGGER.error("Transaction can't happen since one of sender/receiver does not exist for request : {}", transaction.toString());
            throw new Exception("Bad payload");
        }
        Wallet senderWallet = walletRepository.findByUserId(sender.getId());
        Wallet receiverWallet = walletRepository.findByUserId(receiver.getId());

        int amount = transaction.getAmount();

        if(senderWallet.getBalance() < amount){
            LOGGER.error("Not sufficient balance for this transaction {}", transaction.toString());
            throw new Exception("Not sufficient balance");
        }

        senderWallet.setBalance(senderWallet.getBalance()-amount);
        receiverWallet.setBalance(receiverWallet.getBalance()+amount);
        //false-no transaction, true -successful transaction
        transaction.setStatus(Boolean.TRUE);
        LOGGER.info("transaction was successfull with sender {} and receiver {}", sender.toString(), receiver.toString());
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);
        //email service
        return transactionRepository.save(transaction);
    }

    //getBalance

    //addBalance

}

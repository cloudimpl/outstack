/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose
 * Tools | Templates and open the template in the editor.
 */
package com.cloudimpl.outstack.coreImpl;

import java.io.IOException;

/**
 *
 * @author nuwansa
 */
public class EncoderException extends RuntimeException {

  public EncoderException(IOException ex) {
    super(ex);
  }

}

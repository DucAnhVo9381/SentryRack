// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import org.junit.Test;

import ca.kava.it.sentryrack.model.InputValidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for InputValidator (Deliverable 5 #18 a, e).
 * Checks valid and invalid input used by the Login and Sign up screens.
 * @author Anvesh Rachakonda
 * @date 2026-11-26
 */
public class InputValidatorUnitTest {

    // 1. The test account email is accepted
    @Test
    public void isValidEmail_validEmail_returnsTrue() {
        assertTrue(InputValidator.isValidEmail("aaa@bbb.com"));
    }

    // 2. Email with nothing after @ is refused
    @Test
    public void isValidEmail_missingDomain_returnsFalse() {
        assertFalse(InputValidator.isValidEmail("aaa@"));
    }

    // 3. Email without a dot in the domain, without @, or null is refused
    @Test
    public void isValidEmail_otherInvalidEmails_returnFalse() {
        assertFalse(InputValidator.isValidEmail("aaa@bbb"));
        assertFalse(InputValidator.isValidEmail("aaabbb.com"));
        assertFalse(InputValidator.isValidEmail(null));
    }

    // 4. A valid and an invalid email never give the same answer
    @Test
    public void isValidEmail_validAndInvalid_giveDifferentResults() {
        assertNotEquals(InputValidator.isValidEmail("aaa@bbb.com"),
                InputValidator.isValidEmail("aaa@"));
    }


    // 5. The test account password follows all the rules
    @Test
    public void isValidPassword_strongPassword_returnsTrue() {
        assertTrue(InputValidator.isValidPassword("Admin101!"));
    }

    // 6. Passwords missing one rule are refused
    @Test
    public void isValidPassword_weakPasswords_returnFalse() {
        assertFalse(InputValidator.isValidPassword("admin101!"));   // no upper case
        assertFalse(InputValidator.isValidPassword("ADMIN101!"));   // no lower case
        assertFalse(InputValidator.isValidPassword("Adminabc!"));   // no digit
        assertFalse(InputValidator.isValidPassword("Admin1010"));   // no special character
        assertFalse(InputValidator.isValidPassword("Adm101!"));     // shorter than 9
    }

    // 7. Spaces are not allowed inside a password
    @Test
    public void isValidPassword_withSpace_returnsFalse() {
        assertFalse(InputValidator.isValidPassword("Admin 101!"));
    }

    // 8. Confirm password must be exactly the same
    @Test
    public void passwordsMatch_sameAndDifferent() {
        assertTrue(InputValidator.passwordsMatch("Admin101!", "Admin101!"));
        assertFalse(InputValidator.passwordsMatch("Admin101!", "admin101!"));
        assertFalse(InputValidator.passwordsMatch(null, "Admin101!"));
    }
}

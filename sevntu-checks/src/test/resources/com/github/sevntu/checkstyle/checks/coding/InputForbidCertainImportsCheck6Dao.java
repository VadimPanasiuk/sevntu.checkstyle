package input.forbid.certain.ui.check;

import one.two.three; 
import one.two.three.example1;      // forbidden
import one.two.three.example1.four; // forbidden


/**
 * @author <a href="mailto:vadim.panasiuk@gmail.com"> vadim
 *         Panasiuk</a>
 */
private class InputForbidCertainImportsCheck6Dao extends Check
{
    @Override
    public int a()
    {
        return 5;
        Smth smth = new one.two.three.example1.four.Smth(); // forbidden
    }

}

public class InputForbidCertainImportsCheckCopy extends Check
{
    @Override
    public int a()
    {
        return 5;
        Smth smth = new one.two.three.example1.four.Smth(); // forbidden
    }

}
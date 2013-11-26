package input.forbid.certain.imports.check;

import one.two.three; 
import one.two.three.example1;      // forbidden
import one.two.three.example1.four; // forbidden


/**
 * @author <a href="mailto:vadim.panasiuk@gmail.com"> vadim
 *         Panasiuk</a>
 */
public class InputForbidCertainImportsCheck2 extends Check
{
    @Override
    public int a()
    {
        return 5;
        Smth smth = new one.two.three.example1.four.Smth(); // forbidden
    }

}

/*
 * 
 */
package jj.uritemplate;

/**
 * A subclass of {@link Address} to verify that field-level annotation are still being 
 * picked up by the {@link DefaultVarExploder}.
 * 
 * @author <a href="ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision: 1.1 $
 */
public class ExtendedAddress extends Address
{

   public String label;

   /**
    * Create a new ExtendedAddress.
    * 
    */
   public ExtendedAddress()
   {
      super();
   }

   /**
    * Create a new ExtendedAddress.
    * 
    * @param street
    * @param city
    * @param state
    * @param postalCode
    * @param country
    */
   public ExtendedAddress(String street, String city, String state, String postalCode, String country)
   {
      super(street, city, state, postalCode, country);
   }

   /**
    * Get the label.
    * 
    * @return the label.
    */
   public String getLabel()
   {
      return label;
   }

   /**
    * Set the label.
    * 
    * @param label The label to set.
    */
   public void setLabel(String label)
   {
      this.label = label;
   }
}

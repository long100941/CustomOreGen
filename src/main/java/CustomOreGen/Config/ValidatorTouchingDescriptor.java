package CustomOreGen.Config;

import org.w3c.dom.Node;

import CustomOreGen.Util.TouchingDescriptor.TouchingContactType;
import CustomOreGen.Util.TouchingDescriptor.TouchingDirection;
import CustomOreGen.Util.TouchingDescriptor.TouchingVolume;

public class ValidatorTouchingDescriptor extends ValidatorBlockDescriptor {
    private final int MINIMUM_POSSIBLE_TOUCHES = 0;
    // 3x3x3 cube surrounding block
    private final int MAXIMUM_POSSIBLE_TOUCHES = (3 * 3 * 3) - 1;

    private final int DEFAULT_MINIMUM_TOUCHES = 1;
    // default to the maximum otherwise user might create a touching rule and it will be missing some touches 
    private final int DEFAULT_MAXIMUM_TOUCHES = MAXIMUM_POSSIBLE_TOUCHES;
    
    private final TouchingVolume DEFAULT_VOLUME = TouchingVolume.Any;
    private final TouchingContactType DEFAULT_CONTACT_TYPE = TouchingContactType.Face;
    private final TouchingDirection DEFAULT_DIRECTION = TouchingDirection.Any;
    private final boolean DEFAULT_MANDATORY = false;
    private final boolean DEFAULT_NEGATE = false;

    public int minimumTouches = DEFAULT_MINIMUM_TOUCHES;
    public int maximumTouches = DEFAULT_MAXIMUM_TOUCHES;
    public TouchingVolume volume = DEFAULT_VOLUME;
    public TouchingContactType contactType = DEFAULT_CONTACT_TYPE;
    public TouchingDirection direction = DEFAULT_DIRECTION;
    public boolean mandatory = DEFAULT_MANDATORY;
    public boolean negate = DEFAULT_NEGATE;

    protected ValidatorTouchingDescriptor(ValidatorNode parent, Node node) {
        super(parent, node);
    }

    @Override
    protected boolean validateChildren() throws ParserException {
        this.minimumTouches = this.validateNamedAttribute(Integer.class, "minimumTouches", DEFAULT_MINIMUM_TOUCHES,
                true);
        if (this.minimumTouches < MINIMUM_POSSIBLE_TOUCHES || this.minimumTouches > MAXIMUM_POSSIBLE_TOUCHES) {
            throw new ParserException("'minimumTouches' must be between " + Integer.toString(MINIMUM_POSSIBLE_TOUCHES) + " and "
                    + Integer.toString(MAXIMUM_POSSIBLE_TOUCHES) + " inclusive.", this.getNode());
        }

        this.maximumTouches = this.validateNamedAttribute(Integer.class, "maximumTouches", DEFAULT_MAXIMUM_TOUCHES,
                true);
        if (this.maximumTouches < MINIMUM_POSSIBLE_TOUCHES || this.maximumTouches > MAXIMUM_POSSIBLE_TOUCHES) {
            throw new ParserException("'maximumTouches' must be between " + Integer.toString(MINIMUM_POSSIBLE_TOUCHES) + " and "
                    + Integer.toString(MAXIMUM_POSSIBLE_TOUCHES) + " inclusive.", this.getNode());
        }

        if (this.minimumTouches > this.maximumTouches) {
            throw new ParserException("'minimumTouches' must be less than or equal to 'maximumTouches'.",
                    this.getNode());
        }

        this.volume = this.validateNamedAttribute(TouchingVolume.class, "volume", DEFAULT_VOLUME, true);
        this.contactType = this.validateNamedAttribute(TouchingContactType.class, "contactType", DEFAULT_CONTACT_TYPE, true);
        this.direction = this.validateNamedAttribute(TouchingDirection.class, "direction", DEFAULT_DIRECTION, true);

        this.mandatory = this.validateNamedAttribute(Boolean.class, "mandatory", this.mandatory, true);
        this.negate = this.validateNamedAttribute(Boolean.class, "negate", this.negate, true);

        return super.validateChildren();
    }

    public static class Factory implements IValidatorFactory<ValidatorTouchingDescriptor> {
        public ValidatorTouchingDescriptor createValidator(ValidatorNode parent, Node node) {
            return new ValidatorTouchingDescriptor(parent, node);
        }
    }
}

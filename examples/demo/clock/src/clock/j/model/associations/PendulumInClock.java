package clock.j.model.associations;

import clock.j.model.classes.Clock;
import clock.j.model.classes.Pendulum;
import hu.elte.txtuml.api.model.Composition;

public class PendulumInClock extends Composition {
	public class clock extends HiddenContainer<Clock> {}
	public class pendulum extends One<Pendulum> {}
}

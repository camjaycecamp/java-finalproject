package com.mygdx.game;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public enum ZombieState implements State<Zombie>
{
	WANDER() 
	{
		@Override
		public void update(Zombie zombie) 
		{
			if(zombie.detectsPlayer()) 
			{
				zombie.stateMachine.changeState(PURSUE);
			}
			else if (zombie.getHealth() <= 0) 
			{
				zombie.game.waveZombiesDead++;
				zombie.game.totalZombiesDead++;
				zombie.stateMachine.changeState(DIE);
			}
			else 
			{
				zombie.wander();
			}
		}
	},
	
	PURSUE() 
	{
		@Override
		public void update(Zombie zombie) 
		{
			if((zombie.inAttackRange() && zombie.timePassed > 1f)) 
			{
				zombie.setAttackRotation(zombie.getRotation());
				zombie.timePassed = 0f;
				zombie.stateMachine.changeState(ATTACK);
			}
			else if(!zombie.detectsPlayer()) 
			{
				zombie.wanderPosition = 
						new Vector2(zombie.body.getPosition().x + (MathUtils.random()*(11f)-5f), 
						(zombie.body.getPosition().y + MathUtils.random()*(11f)-5f));
				zombie.stateMachine.changeState(WANDER);
			}
			else if (zombie.getHealth() <= 0) 
			{
				zombie.game.waveZombiesDead++;
				zombie.game.totalZombiesDead++;
				zombie.stateMachine.changeState(DIE);
			}
			else
			{
				zombie.pursue();
			}
		}
	},
	
	ATTACK() 
	{
		@Override
		public void update(Zombie zombie) 
		{
			if (zombie.getAttackOpenerAnimation().isAnimationFinished(zombie.timePassed))
			{
				zombie.wanderTimer = 0f;
				zombie.timePassed = 0f;
				zombie.stateMachine.changeState(PURSUE);
			}
			else if (zombie.getHealth() <= 0) 
			{
				zombie.game.waveZombiesDead++;
				zombie.game.totalZombiesDead++;
				zombie.stateMachine.changeState(DIE);
			}
			else 
			{
				zombie.attack();
			}
		}
	},
	
	DIE() 
	{
		@Override
		public void update(Zombie zombie)
		{
			zombie.die();
		}
	};
	
	
	// implemented methods
	@Override
	public void enter(Zombie entity) {}

	@Override
	public void update(Zombie entity) {}

	@Override
	public void exit(Zombie entity) {}

	@Override
	public boolean onMessage(Zombie entity, Telegram telegram) {return false;}
}

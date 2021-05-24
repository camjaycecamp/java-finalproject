package com.mygdx.game;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;

public enum PlayerState implements State<Player>
{
	IDLE() 
	{
		@Override
		public void update(Player player) 
		{
			if (player.getHealth() <= 0) 
			{
				player.deathPosition.set(player.body.getPosition());
				player.stateMachine.changeState(DIE);
			}
			else 
			{
				player.idle();
			}
		}
	},
	
	ATTACK() 
	{
		@Override
		public void update(Player player) 
		{
			if(player.getAttackOpenerAnimation().isAnimationFinished(player.timePassed)) 
			{
				player.timePassed = 0f;
				player.stateMachine.changeState(IDLE);
			}
			else if (player.getHealth() <= 0) 
			{
				player.deathPosition.set(player.body.getPosition());
				player.stateMachine.changeState(DIE);
			}
			else 
			{
				player.attack();
			}
		}
	},
	
	DIE() 
	{
		@Override
		public void update(Player player) 
		{
			player.die();
		}
	};
	

	@Override
	public void enter(Player entity) {}

	@Override
	public void update(Player entity) {}

	@Override
	public void exit(Player entity) {}

	@Override
	public boolean onMessage(Player entity, Telegram telegram) {return false;}
	
}
